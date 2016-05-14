// Copyright 2012 The Chromium Authors. All rights reserved.
// Use of this source code is governed by a BSD-style license that can be
// found in the LICENSE file.

#include "config.h"

#include "CCPrioritizedTextureManager.h"

#include "CCPrioritizedTexture.h"
#include "CCPriorityCalculator.h"
#include "CCProxy.h"
#include "TraceEvent.h"
#include <algorithm>

using namespace std;

namespace cc {

CCPrioritizedTextureManager::CCPrioritizedTextureManager(size_t maxMemoryLimitBytes, int, int pool)
    : m_maxMemoryLimitBytes(maxMemoryLimitBytes)
    , m_memoryUseBytes(0)
    , m_memoryAboveCutoffBytes(0)
    , m_memoryAvailableBytes(0)
    , m_pool(pool)
{
}

CCPrioritizedTextureManager::~CCPrioritizedTextureManager()
{
    while (m_textures.size() > 0)
        unregisterTexture(*m_textures.begin());

    // Each remaining backing is a leaked opengl texture. We don't have the resourceProvider
    // to delete the textures at this time so clearMemory() needs to be called before this.
    while (m_backings.size() > 0)
        destroyBacking(*m_backings.begin(), 0);
}

void CCPrioritizedTextureManager::prioritizeTextures()
{
    TRACE_EVENT0("cc", "CCPrioritizedTextureManager::prioritizeTextures");
    ASSERT(CCProxy::isMainThread());

#if !ASSERT_DISABLED
    assertInvariants();
#endif

    // Sorting textures in this function could be replaced by a slightly
    // modified O(n) quick-select to partition textures rather than
    // sort them (if performance of the sort becomes an issue).

    TextureVector& sortedTextures = m_tempTextureVector;
    sortedTextures.clear();


    // Copy all textures into a vector and sort them.
    for (TextureSet::iterator it = m_textures.begin(); it != m_textures.end(); ++it)
        sortedTextures.append(*it);
    std::sort(sortedTextures.begin(), sortedTextures.end(), compareTextures);

    m_memoryAvailableBytes = m_maxMemoryLimitBytes;
    m_priorityCutoff = CCPriorityCalculator::lowestPriority();
    size_t memoryBytes = 0;
    for (TextureVector::iterator it = sortedTextures.begin(); it != sortedTextures.end(); ++it) {
        if ((*it)->requestPriority() == CCPriorityCalculator::lowestPriority())
            break;

        if ((*it)->isSelfManaged()) {
            // Account for self-managed memory immediately by reducing the memory
            // available (since it never gets acquired).
            size_t newMemoryBytes = memoryBytes + (*it)->bytes();
            if (newMemoryBytes > m_memoryAvailableBytes) {
                m_priorityCutoff = (*it)->requestPriority();
                m_memoryAvailableBytes = memoryBytes;
                break;
            }
            m_memoryAvailableBytes -= (*it)->bytes();
        } else {
            size_t newMemoryBytes = memoryBytes + (*it)->bytes();
            if (newMemoryBytes > m_memoryAvailableBytes) {
                m_priorityCutoff = (*it)->requestPriority();
                break;
            }
            memoryBytes = newMemoryBytes;
        }
    }

    // Only allow textures if they are higher than the cutoff. All textures
    // of the same priority are accepted or rejected together, rather than
    // being partially allowed randomly.
    m_memoryAboveCutoffBytes = 0;
    for (TextureVector::iterator it = sortedTextures.begin(); it != sortedTextures.end(); ++it) {
        bool isAbovePriorityCutoff = CCPriorityCalculator::priorityIsHigher((*it)->requestPriority(), m_priorityCutoff);
        (*it)->setAbovePriorityCutoff(isAbovePriorityCutoff);
        if (isAbovePriorityCutoff && !(*it)->isSelfManaged())
            m_memoryAboveCutoffBytes += (*it)->bytes();
    }
    ASSERT(m_memoryAboveCutoffBytes <= m_memoryAvailableBytes);

    sortedTextures.clear();

    sortBackings();

#if !ASSERT_DISABLED
    assertInvariants();
    ASSERT(memoryAboveCutoffBytes() <= maxMemoryLimitBytes());
#endif
}

void CCPrioritizedTextureManager::sortBackings()
{
    BackingVector& sortedBackings = m_tempBackingVector;
    sortedBackings.clear();

    // Put backings in eviction/recycling order.
    for (BackingSet::iterator it = m_backings.begin(); it != m_backings.end(); ++it)
        sortedBackings.append(*it);
    std::sort(sortedBackings.begin(), sortedBackings.end(), compareBackings);

    for (BackingVector::iterator it = sortedBackings.begin(); it != sortedBackings.end(); ++it) {
        m_backings.remove(*it);
        m_backings.add(*it);
    }

    sortedBackings.clear();

#if !ASSERT_DISABLED
    // Make sure that the backings are sorted as expected
    bool reachedInUse = false;
    bool reachedUnavailable = false;
    for (BackingSet::iterator it = m_backings.begin(); it != m_backings.end(); ++it) {
        CCPrioritizedTexture::Backing* backing = (*it);
        bool isUnavailable = false;
        if (backing->isInUse()) {
            reachedInUse = true;
            isUnavailable = true;
            reachedUnavailable = true;
        }
        if (backing->owner() && backing->owner()->isAbovePriorityCutoff()) {
            isUnavailable = true;
            reachedUnavailable = true;
        }
        if (reachedInUse)
            ASSERT(backing->isInUse());
        if (reachedUnavailable)
            ASSERT(isUnavailable);
    }
#endif
}

void CCPrioritizedTextureManager::markAllBackingsNotInUse()
{
    for (BackingSet::iterator it = m_backings.begin(); it != m_backings.end(); ++it)
        (*it)->setInUse(false);

    // Re-sort the backings so that backings that had been important because they
    // were in-use can now be marked as not-important.
    sortBackings();
}

void CCPrioritizedTextureManager::markLinkedBackingsInUse()
{
    for (BackingSet::iterator it = m_backings.begin(); it != m_backings.end(); ++it)
        (*it)->setInUse(!!(*it)->owner());

    // Re-sort the backings so that the now-in-use backings are counted as the most
    // important.
    sortBackings();
}

void CCPrioritizedTextureManager::clearPriorities()
{
    ASSERT(CCProxy::isMainThread());
    for (TextureSet::iterator it = m_textures.begin(); it != m_textures.end(); ++it) {
        // FIXME: We should remove this and just set all priorities to
        //        CCPriorityCalculator::lowestPriority() once we have priorities
        //        for all textures (we can't currently calculate distances for
        //        off-screen textures).
        (*it)->setRequestPriority(CCPriorityCalculator::lingeringPriority((*it)->requestPriority()));
    }
}

bool CCPrioritizedTextureManager::requestLate(CCPrioritizedTexture* texture)
{
    ASSERT(CCProxy::isMainThread());

    // This is already above cutoff, so don't double count it's memory below.
    if (texture->isAbovePriorityCutoff())
        return true;

    if (CCPriorityCalculator::priorityIsLower(texture->requestPriority(), m_priorityCutoff))
        return false;

    size_t newMemoryBytes = m_memoryAboveCutoffBytes + texture->bytes();
    if (newMemoryBytes > m_memoryAvailableBytes)
        return false;

    m_memoryAboveCutoffBytes = newMemoryBytes;
    texture->setAbovePriorityCutoff(true);
    if (texture->backing()) {
        m_backings.remove(texture->backing());
        m_backings.add(texture->backing());
    }
    return true;
}

void CCPrioritizedTextureManager::acquireBackingTextureIfNeeded(CCPrioritizedTexture* texture, CCResourceProvider* resourceProvider)
{
    ASSERT(CCProxy::isImplThread() && CCProxy::isMainThreadBlocked());
    ASSERT(!texture->isSelfManaged());
    ASSERT(texture->isAbovePriorityCutoff());

    // In theory, we should never be uploading to a texture which has
    // an in-use backing. In practice, this happens regularly.

    if (texture->backing() || !texture->isAbovePriorityCutoff())
        return;

    // Find a backing below, by either recycling or allocating.
    CCPrioritizedTexture::Backing* backing = 0;

    // First try to recycle
    for (BackingSet::iterator it = m_backings.begin(); it != m_backings.end(); ++it) {
        if ((*it)->isInUse())
            break;
        if ((*it)->owner() && (*it)->owner()->isAbovePriorityCutoff())
            break;
        if ((*it)->size() == texture->size() && (*it)->format() == texture->format()) {
            backing = (*it);
            break;
        }
    }

    // Otherwise reduce memory and just allocate a new backing texures.
    if (!backing) {
        reduceMemory(m_memoryAvailableBytes - texture->bytes(), resourceProvider);
        backing = createBacking(texture->size(), texture->format(), resourceProvider);
    }

    // Move the used backing texture to the end of the eviction list.
    if (backing->owner())
        backing->owner()->unlink();
    texture->link(backing);
    m_backings.remove(backing);
    m_backings.add(backing);
}

void CCPrioritizedTextureManager::reduceMemory(size_t limitBytes, CCResourceProvider* resourceProvider)
{
    ASSERT(CCProxy::isImplThread() && CCProxy::isMainThreadBlocked());
    if (memoryUseBytes() <= limitBytes)
        return;

    // Destroy backings until we are below the limit,
    // or until all backings remaining are above the cutoff.
    while (memoryUseBytes() > limitBytes && m_backings.size() > 0) {
        BackingSet::iterator it = m_backings.begin();
        if ((*it)->isInUse())
            break;
        if ((*it)->owner() && (*it)->owner()->isAbovePriorityCutoff())
            break;
        destroyBacking((*it), resourceProvider);
    }
}

void CCPrioritizedTextureManager::reduceMemory(CCResourceProvider* resourceProvider)
{
    ASSERT(CCProxy::isImplThread() && CCProxy::isMainThreadBlocked());
    reduceMemory(m_memoryAvailableBytes, resourceProvider);
    ASSERT(memoryUseBytes() <= maxMemoryLimitBytes());

    // We currently collect backings from deleted textures for later recycling.
    // However, if we do that forever we will always use the max limit even if
    // we really need very little memory. This should probably be solved by reducing the
    // limit externally, but until then this just does some "clean up" of unused
    // backing textures (any more than 10%).
    size_t wastedMemory = 0;
    for (BackingSet::iterator it = m_backings.begin(); it != m_backings.end(); ++it) {
        if ((*it)->owner())
            break;
        wastedMemory += (*it)->bytes();
    }
    size_t tenPercentOfMemory = m_memoryAvailableBytes / 10;
    if (wastedMemory <= tenPercentOfMemory)
        return;
    reduceMemory(memoryUseBytes() - (wastedMemory - tenPercentOfMemory), resourceProvider);
}

void CCPrioritizedTextureManager::clearAllMemory(CCResourceProvider* resourceProvider)
{
    // Unlink and destroy all backing textures.
    while (m_backings.size() > 0) {
        BackingSet::iterator it = m_backings.begin();
        if ((*it)->owner())
            (*it)->owner()->unlink();
        destroyBacking((*it), resourceProvider);
    }
}

void CCPrioritizedTextureManager::allBackingTexturesWereDeleted()
{
    // Same as clearAllMemory, except all our textures were already
    // deleted externally, so we don't delete them. Passing no
    // resourceProvider results in leaking the (now invalid) texture ids.
    clearAllMemory(0);
}

void CCPrioritizedTextureManager::registerTexture(CCPrioritizedTexture* texture)
{
    ASSERT(CCProxy::isMainThread());
    ASSERT(texture);
    ASSERT(!texture->textureManager());
    ASSERT(!texture->backing());
    ASSERT(m_textures.find(texture) == m_textures.end());

    texture->setManagerInternal(this);
    m_textures.add(texture);

}

void CCPrioritizedTextureManager::unregisterTexture(CCPrioritizedTexture* texture)
{
    ASSERT(CCProxy::isMainThread() || (CCProxy::isImplThread() && CCProxy::isMainThreadBlocked()));
    ASSERT(texture);
    ASSERT(m_textures.find(texture) != m_textures.end());

    returnBackingTexture(texture);
    texture->setManagerInternal(0);
    m_textures.remove(texture);
    texture->setAbovePriorityCutoff(false);
}

void CCPrioritizedTextureManager::returnBackingTexture(CCPrioritizedTexture* texture)
{
    ASSERT(CCProxy::isMainThread() || (CCProxy::isImplThread() && CCProxy::isMainThreadBlocked()));
    if (texture->backing()) {
        // Move the backing texture to the front for eviction/recycling and unlink it.
        m_backings.remove(texture->backing());
        m_backings.insertBefore(m_backings.begin(), texture->backing());
        texture->unlink();
    }
}

CCPrioritizedTexture::Backing* CCPrioritizedTextureManager::createBacking(IntSize size, GC3Denum format, CCResourceProvider* resourceProvider)
{
    ASSERT(CCProxy::isImplThread() && CCProxy::isMainThreadBlocked());
    ASSERT(resourceProvider);
    CCResourceProvider::ResourceId resourceId = resourceProvider->createResource(m_pool, size, format, CCResourceProvider::TextureUsageAny);
    CCPrioritizedTexture::Backing* backing = new CCPrioritizedTexture::Backing(resourceId, size, format);
    m_memoryUseBytes += backing->bytes();
    // Put backing texture at the front for eviction, since it isn't in use yet.
    m_backings.insertBefore(m_backings.begin(), backing);
    return backing;
}

void CCPrioritizedTextureManager::destroyBacking(CCPrioritizedTexture::Backing* backing, CCResourceProvider* resourceProvider)
{
    ASSERT(backing);
    ASSERT(!backing->owner() || !backing->owner()->isAbovePriorityCutoff());
    ASSERT(!backing->owner() || !backing->owner()->isSelfManaged());
    ASSERT(m_backings.find(backing) != m_backings.end());

    if (resourceProvider)
        resourceProvider->deleteResource(backing->id());
    if (backing->owner())
        backing->owner()->unlink();
    m_memoryUseBytes -= backing->bytes();
    m_backings.remove(backing);

    delete backing;
}


#if !ASSERT_DISABLED
void CCPrioritizedTextureManager::assertInvariants()
{
    ASSERT(CCProxy::isMainThread());

    // If we hit any of these asserts, there is a bug in this class. To see
    // where the bug is, call this function at the beginning and end of
    // every public function.

    // Backings/textures must be doubly-linked and only to other backings/textures in this manager.
    for (BackingSet::iterator it = m_backings.begin(); it != m_backings.end(); ++it) {
        if ((*it)->owner()) {
            ASSERT(m_textures.find((*it)->owner()) != m_textures.end());
            ASSERT((*it)->owner()->backing() == (*it));
        }
    }
    for (TextureSet::iterator it = m_textures.begin(); it != m_textures.end(); ++it) {
        if ((*it)->backing()) {
            ASSERT(m_backings.find((*it)->backing()) != m_backings.end());
            ASSERT((*it)->backing()->owner() == (*it));
        }
    }
}
#endif


} // namespace cc
