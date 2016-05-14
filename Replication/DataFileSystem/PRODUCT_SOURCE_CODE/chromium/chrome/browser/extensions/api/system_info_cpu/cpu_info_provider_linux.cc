// Copyright (c) 2012 The Chromium Authors. All rights reserved.
// Use of this source code is governed by a BSD-style license that can be
// found in the LICENSE file.

#include "chrome/browser/extensions/api/system_info_cpu/cpu_info_provider.h"

namespace extensions {

bool CpuInfoProvider::QueryCpuTimePerProcessor(std::vector<CpuTime>* times) {
  // TODO(hongbo): Query the cpu time from /proc/stat.
  return false;
}

}  // namespace extensions