// Copyright (c) 2012 The Chromium Authors. All rights reserved.
// Use of this source code is governed by a BSD-style license that can be
// found in the LICENSE file.

#include "content/renderer/browser_plugin/browser_plugin_bindings.h"

#include <cstdlib>
#include <string>

#include "base/bind.h"
#include "base/logging.h"
#include "base/message_loop.h"
#include "base/string16.h"
#include "base/string_split.h"
#include "base/utf_string_conversions.h"
#include "content/renderer/browser_plugin/browser_plugin.h"
#include "third_party/npapi/bindings/npapi.h"
#include "third_party/WebKit/Source/WebKit/chromium/public/WebBindings.h"
#include "third_party/WebKit/Source/WebKit/chromium/public/WebDocument.h"
#include "third_party/WebKit/Source/WebKit/chromium/public/WebDOMMessageEvent.h"
#include "third_party/WebKit/Source/WebKit/chromium/public/WebElement.h"
#include "third_party/WebKit/Source/WebKit/chromium/public/WebFrame.h"
#include "third_party/WebKit/Source/WebKit/chromium/public/WebNode.h"
#include "third_party/WebKit/Source/WebKit/chromium/public/WebPluginContainer.h"
#include "third_party/WebKit/Source/WebKit/chromium/public/platform/WebSerializedScriptValue.h"
#include "third_party/WebKit/Source/WebKit/chromium/public/platform/WebString.h"
#include "v8/include/v8.h"

using WebKit::WebBindings;
using WebKit::WebElement;
using WebKit::WebDOMEvent;
using WebKit::WebDOMMessageEvent;
using WebKit::WebPluginContainer;
using WebKit::WebSerializedScriptValue;
using WebKit::WebString;

namespace content {

namespace {

const char kAddEventListener[] = "addEventListener";
const char kRemoveEventListener[] = "removeEventListener";
const char kSrcAttribute[] = "src";

BrowserPluginBindings* GetBindings(NPObject* object) {
  return static_cast<BrowserPluginBindings::BrowserPluginNPObject*>(object)->
      message_channel;
}

bool IdentifierIsAddEventListener(NPIdentifier identifier) {
  return WebBindings::getStringIdentifier(kAddEventListener) == identifier;
}

bool IdentifierIsRemoveEventListener(NPIdentifier identifier) {
  return WebBindings::getStringIdentifier(kRemoveEventListener) == identifier;
}

bool IdentifierIsSrcAttribute(NPIdentifier identifier) {
  return WebBindings::getStringIdentifier(kSrcAttribute) == identifier;
}

std::string StringFromNPVariant(const NPVariant& variant) {
  if (!NPVARIANT_IS_STRING(variant))
    return std::string();
  const NPString& np_string = NPVARIANT_TO_STRING(variant);
  return std::string(np_string.UTF8Characters, np_string.UTF8Length);
}

string16 String16FromNPVariant(const NPVariant& variant) {
  if (!NPVARIANT_IS_STRING(variant))
    return string16();
  const NPString& np_string = NPVARIANT_TO_STRING(variant);
  string16 wstr;
  if (!UTF8ToUTF16(np_string.UTF8Characters, np_string.UTF8Length, &wstr))
    return string16();
  return wstr;
}

bool StringToNPVariant(const std::string &in, NPVariant *variant) {
  size_t length = in.size();
  NPUTF8 *chars = static_cast<NPUTF8 *>(malloc(length));
  if (!chars) {
    VOID_TO_NPVARIANT(*variant);
    return false;
  }
  memcpy(chars, in.c_str(), length);
  STRINGN_TO_NPVARIANT(chars, length, *variant);
  return true;
}

//------------------------------------------------------------------------------
// Implementations of NPClass functions.  These are here to:
// - Implement src attribute.
//------------------------------------------------------------------------------
NPObject* BrowserPluginBindingsAllocate(NPP npp, NPClass* the_class) {
  return new BrowserPluginBindings::BrowserPluginNPObject;
}

void BrowserPluginBindingsDeallocate(NPObject* object) {
  BrowserPluginBindings::BrowserPluginNPObject* instance =
      static_cast<BrowserPluginBindings::BrowserPluginNPObject*>(object);
  delete instance;
}

bool BrowserPluginBindingsHasMethod(NPObject* np_obj, NPIdentifier name) {
  if (!np_obj)
    return false;

  if (IdentifierIsAddEventListener(name))
    return true;

  if (IdentifierIsRemoveEventListener(name))
    return true;

  return false;
}

bool BrowserPluginBindingsInvoke(NPObject* np_obj, NPIdentifier name,
                                 const NPVariant* args, uint32 arg_count,
                                 NPVariant* result) {
  if (!np_obj)
    return false;

  BrowserPluginBindings* bindings = GetBindings(np_obj);
  if (!bindings)
    return false;

  if (IdentifierIsAddEventListener(name) && (arg_count == 2)) {
    std::string event_name = StringFromNPVariant(args[0]);
    if (event_name.empty())
      return false;

    v8::Local<v8::Value> value =
        v8::Local<v8::Value>::New(WebBindings::toV8Value(&args[1]));
    if (value.IsEmpty() || !value->IsFunction())
      return false;

    v8::Local<v8::Function> function = v8::Local<v8::Function>::Cast(value);
    return bindings->instance()->AddEventListener(event_name, function);
  }

  if (IdentifierIsRemoveEventListener(name) && arg_count == 2) {
    std::string event_name = StringFromNPVariant(args[0]);
    if (event_name.empty())
      return false;

    v8::Local<v8::Value> value =
        v8::Local<v8::Value>::New(WebBindings::toV8Value(&args[1]));

    if (value.IsEmpty() || !value->IsFunction())
      return false;

    v8::Local<v8::Function> function = v8::Local<v8::Function>::Cast(value);
    return bindings->instance()->RemoveEventListener(event_name, function);
  }

  return false;
}

bool BrowserPluginBindingsInvokeDefault(NPObject* np_obj,
                                        const NPVariant* args,
                                        uint32 arg_count,
                                        NPVariant* result) {
  NOTIMPLEMENTED();
  return false;
}

bool BrowserPluginBindingsHasProperty(NPObject* np_obj, NPIdentifier name) {
  return IdentifierIsSrcAttribute(name);
}

bool BrowserPluginBindingsGetProperty(NPObject* np_obj, NPIdentifier name,
                                      NPVariant* result) {
  if (!np_obj)
    return false;

  if (IdentifierIsAddEventListener(name) ||
      IdentifierIsRemoveEventListener(name))
    return false;

  if (IdentifierIsSrcAttribute(name)) {
    BrowserPluginBindings* bindings = GetBindings(np_obj);
    if (!bindings)
      return false;
    std::string src = bindings->instance()->GetSrcAttribute();
    return StringToNPVariant(src, result);
  }

  return false;
}

bool BrowserPluginBindingsSetProperty(NPObject* np_obj, NPIdentifier name,
                                      const NPVariant* variant) {
  if (!np_obj)
    return false;

  if (IdentifierIsSrcAttribute(name)) {
    std::string src = StringFromNPVariant(*variant);
    BrowserPluginBindings* bindings = GetBindings(np_obj);
    if (!bindings)
      return false;
    bindings->instance()->SetSrcAttribute(src);
    return true;
  }
  return false;
}

bool BrowserPluginBindingsEnumerate(NPObject *np_obj, NPIdentifier **value,
                                    uint32_t *count) {
  NOTIMPLEMENTED();
  return true;
}

NPClass browser_plugin_message_class = {
  NP_CLASS_STRUCT_VERSION,
  &BrowserPluginBindingsAllocate,
  &BrowserPluginBindingsDeallocate,
  NULL,
  &BrowserPluginBindingsHasMethod,
  &BrowserPluginBindingsInvoke,
  &BrowserPluginBindingsInvokeDefault,
  &BrowserPluginBindingsHasProperty,
  &BrowserPluginBindingsGetProperty,
  &BrowserPluginBindingsSetProperty,
  NULL,
  &BrowserPluginBindingsEnumerate,
};

}  // namespace

// BrowserPluginBindings ------------------------------------------------------

BrowserPluginBindings::BrowserPluginNPObject::BrowserPluginNPObject() {
}

BrowserPluginBindings::BrowserPluginNPObject::~BrowserPluginNPObject() {
}

BrowserPluginBindings::BrowserPluginBindings(BrowserPlugin* instance)
    : instance_(instance),
      np_object_(NULL),
      ALLOW_THIS_IN_INITIALIZER_LIST(weak_ptr_factory_(this)) {
  NPObject* obj =
      WebBindings::createObject(NULL, &browser_plugin_message_class);
  np_object_ = static_cast<BrowserPluginBindings::BrowserPluginNPObject*>(obj);
  np_object_->message_channel = weak_ptr_factory_.GetWeakPtr();
}

BrowserPluginBindings::~BrowserPluginBindings() {
  WebBindings::releaseObject(np_object_);
}

}  // namespace content
