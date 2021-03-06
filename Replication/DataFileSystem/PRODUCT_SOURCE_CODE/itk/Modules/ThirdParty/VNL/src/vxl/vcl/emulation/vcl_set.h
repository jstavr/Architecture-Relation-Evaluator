/*
 *
 * Copyright (c) 1994
 * Hewlett-Packard Company
 *
 * Permission to use, copy, modify, distribute and sell this software
 * and its documentation for any purpose is hereby granted without fee,
 * provided that the above copyright notice appear in all copies and
 * that both that copyright notice and this permission notice appear
 * in supporting documentation.  Hewlett-Packard Company makes no
 * representations about the suitability of this software for any
 * purpose.  It is provided "as is" without express or implied warranty.
 *
 *
 * Copyright (c) 1996
 * Silicon Graphics Computer Systems, Inc.
 *
 * Permission to use, copy, modify, distribute and sell this software
 * and its documentation for any purpose is hereby granted without fee,
 * provided that the above copyright notice appear in all copies and
 * that both that copyright notice and this permission notice appear
 * in supporting documentation.  Silicon Graphics makes no
 * representations about the suitability of this software for any
 * purpose.  It is provided "as is" without express or implied warranty.
 */

/*
 *
 * Copyright (c) 1997
 * Moscow Center for SPARC Technology
 *
 * Permission to use, copy, modify, distribute and sell this software
 * and its documentation for any purpose is hereby granted without fee,
 * provided that the above copyright notice appear in all copies and
 * that both that copyright notice and this permission notice appear
 * in supporting documentation.  Moscow Center for SPARC Technology makes no
 * representations about the suitability of this software for any
 * purpose.  It is provided "as is" without express or implied warranty.
 *
 */

#ifndef vcl_emulation_set_h
#define vcl_emulation_set_h

#include "vcl_tree.h"

__BEGIN_STL_FULL_NAMESPACE
#define vcl_set __WORKAROUND_RENAME(vcl_set)
template <class Key, VCL_DFL_TMPL_PARAM_STLDECL(Compare,vcl_less<Key>),
                     VCL_DFL_TYPE_PARAM_STLDECL(Alloc,vcl_alloc) >
class vcl_set
{
  typedef vcl_set<Key, Compare, Alloc> self;
 public:
  typedef Key key_type;
  typedef Key value_type;
  typedef Compare key_compare;
  typedef Compare value_compare;
  typedef rb_tree<key_type, value_type,
                  vcl_identity<value_type>, key_compare, Alloc> rep_type;
  typedef typename rep_type::const_pointer   pointer;
  typedef typename rep_type::const_reference reference;
  typedef typename rep_type::const_reference const_reference;
  typedef typename rep_type::const_iterator  const_iterator;
  // SunPro 4.1, 4.0.1 bug
#  ifdef __SUNPRO_CC
  typedef const_iterator iterator;
#  else
  typedef typename rep_type::const_iterator iterator;
#  endif
  typedef typename rep_type::const_reverse_iterator reverse_iterator;
  typedef typename rep_type::const_reverse_iterator const_reverse_iterator;
  typedef typename rep_type::size_type size_type;
  typedef typename rep_type::difference_type difference_type;
 private:
  rep_type t;  // red-black vcl_tree representing vcl_set
 public:
  // allocation/deallocation

  vcl_set() : t(Compare()) {}
  explicit vcl_set(const Compare& comp) : t(comp) {}
  vcl_set(const value_type* first, const value_type* last) : t(Compare()) { t.insert_unique(first, last); }
  vcl_set(const value_type* first, const value_type* last, const Compare& comp) : t(comp) { t.insert_unique(first, last); }
  vcl_set(const_iterator first, const_iterator last ) : t(Compare()) { t.insert_unique(first, last); }
  vcl_set(const_iterator first, const_iterator last, const Compare& comp) : t(comp) { t.insert_unique(first, last); }
  vcl_set(const self& x) : t(x.t) {}
  self& operator=(const self& x) { t = x.t; return *this; }

  // accessors:

  key_compare key_comp() const { return t.key_comp(); }
  value_compare value_comp() const { return t.key_comp(); }
  iterator begin() const { return t.begin(); }
  iterator end() const { return t.end(); }
  reverse_iterator rbegin() const { return t.rbegin(); }
  reverse_iterator rend() const { return t.rend(); }
  bool empty() const { return t.empty(); }
  size_type size() const { return t.size(); }
  size_type max_size() const { return t.max_size(); }
  void swap(self& x) { t.swap(x.t); }

  // insert/erase
  typedef  vcl_pair<iterator, bool> pair_iterator_bool;
  vcl_pair<iterator,bool> insert(const value_type& x) {
#ifdef VC50
    vcl_pair< rep_type::iterator, bool> p = t.insert_unique(x);
#else
    vcl_pair<typename rep_type::iterator, bool> p = t.insert_unique(x);
#endif
    return vcl_pair<iterator, bool>(p.first, p.second);
  }
  iterator insert(iterator position, const value_type& x) { return t.insert_unique((typename rep_type::iterator&)position, x); }
  void insert(const_iterator first, const_iterator last) { t.insert_unique(first, last); }
  void insert(const value_type* first, const value_type* last) { t.insert_unique(first, last); }
  void erase(iterator position) { t.erase((typename rep_type::iterator&)position); }
  size_type erase(const key_type& x) { return t.erase(x); }
  void erase(iterator first, iterator last)
  {
      t.erase((typename rep_type::iterator&)first,
              (typename rep_type::iterator&)last);
  }
  void clear() { t.clear(); }

  // vcl_set operations:

  iterator find(const key_type& x) const { return t.find(x); }
  size_type count(const key_type& x) const { return t.count(x); }
  iterator lower_bound(const key_type& x) const { return t.lower_bound(x); }
  iterator upper_bound(const key_type& x) const { return t.upper_bound(x); }
  vcl_pair<iterator,iterator> equal_range(const key_type& x) const { return t.equal_range(x); }
  bool operator==(const self& y) const { return t==y.t; }
  bool operator< (const self& y) const { return t< y.t; }
};
__END_STL_FULL_NAMESPACE

// do a cleanup
# undef vcl_set
// provide a way to access full functionality
# define __set__  __FULL_NAME(vcl_set)

# if defined (__STL_CLASS_PARTIAL_SPECIALIZATION )
template <class Key, class Compare, class Alloc>
inline void swap(__set__<Key, Compare, Alloc>& a,
                 __set__<Key, Compare, Alloc>& b) { a.swap(b); }
# endif

# ifndef __STL_DEFAULT_TYPE_PARAM

// provide a "default" vcl_set adaptor
template <class Key, class Compare>
class vcl_set : public __set__<Key, Compare, vcl_alloc>
{
  typedef vcl_set<Key, Compare> self;
 public:
  typedef __set__<Key, Compare, vcl_alloc> super;
  __CONTAINER_SUPER_TYPEDEFS
  // copy & assignment from super
  __IMPORT_SUPER_COPY_ASSIGNMENT(vcl_set)
  // specific constructors
  explicit vcl_set() : super(Compare()) {}
  explicit vcl_set(const Compare& comp) : super(comp) {}
  vcl_set(const value_type* first, const value_type* last) :
      super(first, last, Compare()) { }
  vcl_set(const value_type* first, const value_type* last,
          const Compare& comp) : super(first, last, comp) { }
  vcl_set(const_iterator first, const_iterator last) :
      super(first, last, Compare()) { }
  vcl_set(const_iterator first, const_iterator last,
          const Compare& comp) : super(first, last, comp) { }
};

#  if defined (__STL_BASE_MATCH_BUG)
template <class Key, class Compare>
inline bool operator==(const vcl_set<Key, Compare>& x,
                       const vcl_set<Key, Compare>& y)
{
  typedef  vcl_set<Key, Compare>::super super;
  return operator==((const super&)x,(const super&)y);
}

template <class Key, class Compare>
inline bool operator<(const vcl_set<Key, Compare>& x,
                      const vcl_set<Key, Compare>& y)
{
  typedef  vcl_set<Key, Compare>::super super;
  return operator < ((const super&)x , (const super&)y);
}
#  endif
# endif /*  __STL_DEFAULT_TYPE_PARAM */

#endif // vcl_emulation_set_h
