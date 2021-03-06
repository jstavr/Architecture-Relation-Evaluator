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
/*=========================================================================
 *
 *  Portions of this file are subject to the VTK Toolkit Version 3 copyright.
 *
 *  Copyright (c) Ken Martin, Will Schroeder, Bill Lorensen
 *
 *  For complete copyright, license and disclaimer of warranty information
 *  please refer to the NOTICE file at the top of the ITK source tree.
 *
 *=========================================================================*/
#ifndef __itkThresholdImageFilter_h
#define __itkThresholdImageFilter_h

#include "itkInPlaceImageFilter.h"

#include "itkConceptChecking.h"

namespace itk
{
/** \class ThresholdImageFilter
 * \brief Set image values to a user-specified value if they are below,
 * above, or between simple threshold values.
 *
 * ThresholdImageFilter sets image values to a user-specified "outside"
 * value (by default, "black") if the image values are below, above, or
 * between simple threshold values.
 *
 * The pixels must support the operators >= and <=.
 *
 * \ingroup IntensityImageFilters MultiThreaded
 * \ingroup ITKThresholding
 *
 * \wiki
 * \wikiexample{ImageProcessing/ThresholdImageFilter,Threshold an image}
 * \endwiki
 */
template< typename TImage >
class ThresholdImageFilter:public InPlaceImageFilter< TImage, TImage >
{
public:
  /** Standard class typedefs. */
  typedef ThresholdImageFilter                 Self;
  typedef InPlaceImageFilter< TImage, TImage > Superclass;
  typedef SmartPointer< Self >                 Pointer;
  typedef SmartPointer< const Self >           ConstPointer;

  /** Method for creation through the object factory. */
  itkNewMacro(Self);

  /** Run-time type information (and related methods). */
  itkTypeMacro(ThresholdImageFilter, InPlaceImageFilter);

  /** Typedef to describe the type of pixel. */
  typedef typename TImage::PixelType PixelType;

  /** The pixel type must support comparison operators. */
#ifdef ITK_USE_CONCEPT_CHECKING
  // Begin concept checking
  itkConceptMacro( PixelTypeComparableCheck,
                   ( Concept::Comparable< PixelType > ) );
  itkConceptMacro( PixelTypeOStreamWritableCheck,
                   ( Concept::OStreamWritable< PixelType > ) );
  // End concept checking
#endif

  /** Set the "outside" pixel value. The default value
   * NumericTraits<PixelType>::Zero. */
  itkSetMacro(OutsideValue, PixelType);

  /** Get the "outside" pixel value. */
  itkGetConstMacro(OutsideValue, PixelType);

  /** The values greater than or equal to the value are set to OutsideValue. */
  void ThresholdAbove(const PixelType & thresh);

  /** The values less than or equal to the value are set to OutsideValue. */
  void ThresholdBelow(const PixelType & thresh);

  /** The values outside the range are set to OutsideValue. */
  void ThresholdOutside(const PixelType & lower, const PixelType & upper);

  /** Set/Get methods to set the lower threshold */
  itkSetMacro(Lower, PixelType);
  itkGetConstMacro(Lower, PixelType);

  /** Set/Get methods to set the upper threshold */
  itkSetMacro(Upper, PixelType);
  itkGetConstMacro(Upper, PixelType);

  /** Some additional typedefs.  */
  typedef TImage                                InputImageType;
  typedef typename InputImageType::ConstPointer InputImagePointer;
  typedef typename InputImageType::RegionType   InputImageRegionType;
  typedef typename InputImageType::PixelType    InputImagePixelType;

  /** Some additional typedefs.  */
  typedef TImage                               OutputImageType;
  typedef typename OutputImageType::Pointer    OutputImagePointer;
  typedef typename OutputImageType::RegionType OutputImageRegionType;
  typedef typename OutputImageType::PixelType  OutputImagePixelType;

protected:
  ThresholdImageFilter();
  ~ThresholdImageFilter() {}
  void PrintSelf(std::ostream & os, Indent indent) const;

  /** ThresholdImageFilter can be implemented as a multithreaded filter.
   * Therefore, this implementation provides a ThreadedGenerateData() routine
   * which is called for each processing thread. The output image data is
   * allocated automatically by the superclass prior to calling
   * ThreadedGenerateData().  ThreadedGenerateData can only write to the
   * portion of the output image specified by the parameter
   * "outputRegionForThread"
   *
   * \sa ImageToImageFilter::ThreadedGenerateData(),
   *     ImageToImageFilter::GenerateData()  */
  void ThreadedGenerateData(const OutputImageRegionType & outputRegionForThread,
                            ThreadIdType threadId);

private:
  ThresholdImageFilter(const Self &); //purposely not implemented
  void operator=(const Self &);       //purposely not implemented

  PixelType m_OutsideValue;
  PixelType m_Lower;
  PixelType m_Upper;
};
} // end namespace itk

#ifndef ITK_MANUAL_INSTANTIATION
#include "itkThresholdImageFilter.hxx"
#endif

#endif
