/*=========================================================================
 *
 *  Copyright Insight Software Consortium
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *         http://www.apache.org/licenses/LICENSE-2.0.txt
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 *
 *=========================================================================*/
#ifndef __itkNumberToString_h
#define __itkNumberToString_h

#include "double-conversion.h"
#include "itkMacro.h"
#include <string>

namespace itk
{
/** \class NumberToString
 * \brief Convert floating and fixed point numbers to strings
 *
 * This class uses the double-conversion library to floating point and
 * fixed point numbers to ASCII versions that are represented without
 * numerical precision errors.
 *
 * Typical use:
 *  \#include "itkNumberToStrong.h"
 *  NumberToString<float> convert;
 *  float a = 1.0f/3.0f;
 *  std::cout << convert(a) << std::endl;
 *
 * \ingroup ITKCommon
 */
template< typename TValueType>
class NumberToString
{
public:
  NumberToString() :
    m_DoubleToStringConverter(double_conversion::DoubleToStringConverter::EcmaScriptConverter())
    {
    }
  std::string operator() (TValueType val);

private:
  NumberToString & operator=(const NumberToString &); // not defined
  const double_conversion::DoubleToStringConverter &m_DoubleToStringConverter;
};

// declaration of specialization
template<> ITKCommon_EXPORT std::string NumberToString<double>::operator()(double val);
template<> ITKCommon_EXPORT std::string NumberToString<float>::operator()(float val);

} // namespace itk

#ifndef ITK_MANUAL_INSTANTIATION
#include "itkNumberToString.hxx"
#endif
#endif
