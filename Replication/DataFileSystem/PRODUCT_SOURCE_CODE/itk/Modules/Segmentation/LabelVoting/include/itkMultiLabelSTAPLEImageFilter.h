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
#ifndef __itkMultiLabelSTAPLEImageFilter_h
#define __itkMultiLabelSTAPLEImageFilter_h

#include "itkImage.h"
#include "itkImageToImageFilter.h"

#include "itkImageRegionIterator.h"
#include "itkImageRegionConstIterator.h"

#include "vector"
#include "itkArray.h"
#include "itkArray2D.h"

namespace itk
{
/** \class MultiLabelSTAPLEImageFilter
 *
 * \brief This filter performs a pixelwise combination of an arbitrary number
 * of input images, where each of them represents a segmentation of the same
 * scene (i.e., image).
 *
 * The labelings in the images are weighted relative to each other based on
 * their "performance" as estimated by an expectation-maximization
 * algorithm. In the process, a ground truth segmentation is estimated, and
 * the estimated performances of the individual segmentations are relative to
 * this estimated ground truth.
 *
 * The algorithm is based on the binary STAPLE algorithm by Warfield et al. as
 * published originally in
 *
 * S. Warfield, K. Zou, W. Wells, "Validation of image segmentation and expert
 * quality with an expectation-maximization algorithm" in MICCAI 2002: Fifth
 * International Conference on Medical Image Computing and Computer-Assisted
 * Intervention, Springer-Verlag, Heidelberg, Germany, 2002, pp. 298-306
 *
 * The multi-label algorithm implemented here is described in detail in
 *
 * T. Rohlfing, D. B. Russakoff, and C. R. Maurer, Jr., "Performance-based
 * classifier combination in atlas-based image segmentation using
 * expectation-maximization parameter estimation," IEEE Transactions on
 * Medical Imaging, vol. 23, pp. 983-994, Aug. 2004.
 *
 * \par INPUTS
 * All input volumes to this filter must be segmentations of an image,
 * that is, they must have discrete pixel values where each value represents
 * a different segmented object.
 *
 * Input volumes must all contain the same size RequestedRegions. Not all
 * input images must contain all possible labels, but all label values must
 * have the same meaning in all images.
 *
 * The filter can optionally be provided with estimates for the a priori class
 * probabilities through the SetPriorProbabilities function. If no estimate is
 * provided, one is automatically generated by analyzing the relative
 * frequencies of the labels in the input images.
 *
 * \par OUTPUTS
 * The filter produces a single output volume. Each output pixel
 * contains the label that has the highest probability of being the correct
 * label, based on the performance models of the individual segmentations.
 * If the maximum probaility is not unique, i.e., if more than one label have
 * a maximum probability, then an "undecided" label is assigned to that output
 * pixel.
 *
 * By default, the label used for undecided pixels is the maximum label value
 * used in the input images plus one. Since it is possible for an image with
 * 8 bit pixel values to use all 256 possible label values, it is permissible
 * to combine 8 bit (i.e., byte) images into a 16 bit (i.e., short) output
 * image.
 *
 * In addition to the combined image, the estimated confusion matrices for
 * each of the input segmentations can be obtained through the
 * GetConfusionMatrix member function.
 *
 * \par PARAMETERS
 * The label used for "undecided" labels can be set using
 * SetLabelForUndecidedPixels. This functionality can be unset by calling
 * UnsetLabelForUndecidedPixels.
 *
 * A termination threshold for the EM iteration can be defined by calling
 * SetTerminationUpdateThreshold. The iteration terminates once no single
 * parameter of any confusion matrix changes by less than this
 * threshold. Alternatively, a maximum number of iterations can be specified
 * by calling SetMaximumNumberOfIterations. The algorithm may still terminate
 * after a smaller number of iterations if the termination threshold criterion
 * is satisfied.
 *
 * \par EVENTS
 * This filter invokes IterationEvent() at each iteration of the E-M
 * algorithm. Setting the AbortGenerateData() flag will cause the algorithm to
 * halt after the current iteration and produce results just as if it had
 * converged. The algorithm makes no attempt to report its progress since the
 * number of iterations needed cannot be known in advance.
 *
 * \author Torsten Rohlfing, SRI International, Neuroscience Program
 *
 * \ingroup ITKLabelVoting
 */
template <typename TInputImage, typename TOutputImage = TInputImage, typename TWeights = float >
class MultiLabelSTAPLEImageFilter :
    public ImageToImageFilter< TInputImage, TOutputImage >
{
public:
  /** Standard class typedefs. */
  typedef MultiLabelSTAPLEImageFilter                     Self;
  typedef ImageToImageFilter< TInputImage, TOutputImage > Superclass;
  typedef SmartPointer< Self >                            Pointer;
  typedef SmartPointer< const Self >                      ConstPointer;

  /** Method for creation through the object factory. */
  itkNewMacro(Self);

  /** Run-time type information (and related methods) */
  itkTypeMacro(MultiLabelSTAPLEImageFilter, ImageToImageFilter);

  /** Extract some information from the image types.  Dimensionality
   * of the two images is assumed to be the same. */
  typedef typename TOutputImage::PixelType OutputPixelType;
  typedef typename TInputImage::PixelType  InputPixelType;

  /** Extract some information from the image types.  Dimensionality
   * of the two images is assumed to be the same. */
  itkStaticConstMacro(ImageDimension, unsigned int,
                      TOutputImage::ImageDimension);

  /** Image typedef support */
  typedef TInputImage                       InputImageType;
  typedef TOutputImage                      OutputImageType;
  typedef typename InputImageType::Pointer  InputImagePointer;
  typedef typename OutputImageType::Pointer OutputImagePointer;

  /** Superclass typedefs. */
  typedef typename Superclass::OutputImageRegionType OutputImageRegionType;

  /** Iterator types. */
  typedef ImageRegionConstIterator< TInputImage > InputConstIteratorType;
  typedef ImageRegionIterator< TOutputImage >     OutputIteratorType;

  /** Confusion matrix typedefs. */
  typedef TWeights             WeightsType;
  typedef Array2D<WeightsType> ConfusionMatrixType;
  typedef Array<WeightsType>   PriorProbabilitiesType;

  /** Set maximum number of iterations.
    */
  void SetMaximumNumberOfIterations( const unsigned int mit )
  {
    this->m_MaximumNumberOfIterations = mit;
    this->m_HasMaximumNumberOfIterations = true;
    this->Modified();
  }

  /** Unset label value for undecided pixels and turn on automatic selection.
    */
  void UnsetMaximumNumberOfIterations()
  {
    if ( this->m_HasMaximumNumberOfIterations )
      {
      this->m_HasMaximumNumberOfIterations = false;
      this->Modified();
      }
  }

  /** Set termination threshold based on confusion matrix parameter updates.
    */
  void SetTerminationUpdateThreshold( const TWeights thresh )
  {
    this->m_TerminationUpdateThreshold = thresh;
    this->Modified();
  }

  /** Set label value for undecided pixels.
    */
  void SetLabelForUndecidedPixels( const OutputPixelType l )
  {
    this->m_LabelForUndecidedPixels = l;
    this->m_HasLabelForUndecidedPixels = true;
    this->Modified();
  }

  /** Get label value used for undecided pixels.
    * After updating the filter, this function returns the actual label value
    * used for undecided pixels in the current output. Note that this value
    * is overwritten when SetLabelForUndecidedPixels is called and the new
    * value only becomes effective upon the next filter update.
    */
  OutputPixelType GetLabelForUndecidedPixels() const
  {
    return this->m_LabelForUndecidedPixels;
  }

  /** Unset label value for undecided pixels and turn on automatic selection.
    */
  void UnsetLabelForUndecidedPixels()
  {
    if ( this->m_HasLabelForUndecidedPixels )
      {
      this->m_HasLabelForUndecidedPixels = false;
      this->Modified();
      }
  }

  /** Set label value for undecided pixels.
    */
  void SetPriorProbabilities( const PriorProbabilitiesType& ppa )
  {
    this->m_PriorProbabilities = ppa;
    this->m_HasPriorProbabilities = true;
    this->Modified();
  }

  /** Get prior class probabilities.
    * After updating the filter, this function returns the actual prior class
    * probabilities. If these were not previously set by a call to
    * SetPriorProbabilities, then they are estimated from the input
    * segmentations and the result is available through this function.
    */
  PriorProbabilitiesType GetPriorProbabilities() const
  {
    return this->m_PriorProbabilities;
  }

  /** Unset prior class probabilities and turn on automatic estimation.
    */
  void UnsetPriorProbabilities()
  {
    if ( this->m_HasPriorProbabilities )
      {
      this->m_HasPriorProbabilities = false;
      this->Modified();
      }
  }

  /** Get confusion matrix for the i-th input segmentation.
    */
  ConfusionMatrixType GetConfusionMatrix( const unsigned int i )
  {
    return this->m_ConfusionMatrixArray[i];
  }

protected:
  MultiLabelSTAPLEImageFilter()
  {
    this->m_HasLabelForUndecidedPixels = false;
    this->m_HasPriorProbabilities = false;
    this->m_HasMaximumNumberOfIterations = false;
    this->m_TerminationUpdateThreshold = 1e-5;
  }
  virtual ~MultiLabelSTAPLEImageFilter() {}

  void GenerateData();

  void PrintSelf(std::ostream&, Indent) const;

  /** Determine maximum value among all input images' pixels */
  typename TInputImage::PixelType ComputeMaximumInputValue();

  // Override since the filter needs all the data for the algorithm
  void GenerateInputRequestedRegion();

  // Override since the filter produces all of its output
  void EnlargeOutputRequestedRegion( DataObject * );

private:
  MultiLabelSTAPLEImageFilter(const Self&); //purposely not implemented
  void operator=(const Self&); //purposely not implemented

  size_t m_TotalLabelCount;

  OutputPixelType    m_LabelForUndecidedPixels;
  bool               m_HasLabelForUndecidedPixels;

  bool                   m_HasPriorProbabilities;
  PriorProbabilitiesType m_PriorProbabilities;

  void InitializePriorProbabilities();

  std::vector<ConfusionMatrixType> m_ConfusionMatrixArray;
  std::vector<ConfusionMatrixType> m_UpdatedConfusionMatrixArray;

  void AllocateConfusionMatrixArray();
  void InitializeConfusionMatrixArrayFromVoting();

  bool         m_HasMaximumNumberOfIterations;
  unsigned int m_MaximumNumberOfIterations;

  TWeights m_TerminationUpdateThreshold;
};

} // end namespace itk

#ifndef ITK_MANUAL_INSTANTIATION
#include "itkMultiLabelSTAPLEImageFilter.hxx"
#endif

#endif
