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
#ifndef __itkBayesianClassifierImageFilter_hxx
#define __itkBayesianClassifierImageFilter_hxx

#include "itkBayesianClassifierImageFilter.h"
#include "itkImageRegionConstIterator.h"

namespace itk
{
/**
 *  Constructor
 */
template< typename TInputVectorImage, typename TLabelsType,
          typename TPosteriorsPrecisionType, typename TPriorsPrecisionType >
BayesianClassifierImageFilter< TInputVectorImage, TLabelsType,
                               TPosteriorsPrecisionType, TPriorsPrecisionType >
::BayesianClassifierImageFilter()
{
  m_UserProvidedPriors = false;
  m_UserProvidedSmoothingFilter = false;
  this->SetNumberOfRequiredOutputs(2);
  m_NumberOfSmoothingIterations = 0;
  m_SmoothingFilter = NULL;
  PosteriorsImagePointer p =
    static_cast< PosteriorsImageType * >( this->MakeOutput(1).GetPointer() );
  this->SetNthOutput( 1, p.GetPointer() );
}

/**
 *  Print Self Method
 */
template< typename TInputVectorImage, typename TLabelsType,
          typename TPosteriorsPrecisionType, typename TPriorsPrecisionType >
void
BayesianClassifierImageFilter< TInputVectorImage, TLabelsType,
                               TPosteriorsPrecisionType, TPriorsPrecisionType >
::PrintSelf(std::ostream & os, Indent indent) const
{
  Superclass::PrintSelf(os, indent);

  os << indent << "User provided priors =  " << m_UserProvidedPriors << std::endl;
  os << indent << "User provided smooting filter =  " << m_UserProvidedSmoothingFilter << std::endl;
  os << indent << "Smoothing filter pointer =  " << m_SmoothingFilter.GetPointer() << std::endl;
  os << indent << "Number of smoothing iterations =  " << m_NumberOfSmoothingIterations << std::endl;
}

/**
 * Generate Data method is where the classification (and smoothing) is performed.
 */
template< typename TInputVectorImage, typename TLabelsType,
          typename TPosteriorsPrecisionType, typename TPriorsPrecisionType >
void
BayesianClassifierImageFilter< TInputVectorImage, TLabelsType,
                               TPosteriorsPrecisionType, TPriorsPrecisionType >
::GenerateData()
{
  // Setup input image
  const InputImageType *membershipImage = this->GetInput();

  // Setup general parameters
  const unsigned int numberOfClasses = membershipImage->GetVectorLength();

  if ( numberOfClasses == 0 )
    {
    itkExceptionMacro("The number of components in the input Membership image is Zero !");
    return;
    }

  this->AllocateOutputs();

  this->ComputeBayesRule();

  if ( m_UserProvidedSmoothingFilter )
    {
    this->NormalizeAndSmoothPosteriors();
    }

  this->ClassifyBasedOnPosteriors();
}

template< typename TInputVectorImage, typename TLabelsType,
          typename TPosteriorsPrecisionType, typename TPriorsPrecisionType >
typename BayesianClassifierImageFilter< TInputVectorImage, TLabelsType,
                                        TPosteriorsPrecisionType, TPriorsPrecisionType >
::PosteriorsImageType *
BayesianClassifierImageFilter< TInputVectorImage, TLabelsType,
                               TPosteriorsPrecisionType, TPriorsPrecisionType >
::GetPosteriorImage()
{
  PosteriorsImageType *ptr = dynamic_cast< PosteriorsImageType * >(
    this->ProcessObject::GetOutput(1) );

  return ptr;
}

template< typename TInputVectorImage, typename TLabelsType,
          typename TPosteriorsPrecisionType, typename TPriorsPrecisionType >
typename BayesianClassifierImageFilter< TInputVectorImage, TLabelsType,
                                        TPosteriorsPrecisionType, TPriorsPrecisionType >
::DataObjectPointer
BayesianClassifierImageFilter< TInputVectorImage, TLabelsType,
                               TPosteriorsPrecisionType, TPriorsPrecisionType >
::MakeOutput(DataObjectPointerArraySizeType idx)
{
  if  ( idx == 1 )
    {
    return PosteriorsImageType::New().GetPointer();
    }
  return Superclass::MakeOutput(idx);
}

template< typename TInputVectorImage, typename TLabelsType,
          typename TPosteriorsPrecisionType, typename TPriorsPrecisionType >
void
BayesianClassifierImageFilter< TInputVectorImage, TLabelsType,
                               TPosteriorsPrecisionType, TPriorsPrecisionType >
::GenerateOutputInformation(void)
{
  Superclass::GenerateOutputInformation();

  if ( !this->GetPosteriorImage() )
    {
    return;
    }

  // the vector length is part of the output information that must be
  // updated here
  this->GetPosteriorImage()->SetVectorLength( this->GetInput()->GetVectorLength() );
}

/**
 * Compute the posteriors using the Bayes rule. If no priors are available,
 * then the posteriors are just a copy of the memberships.  */
template< typename TInputVectorImage, typename TLabelsType,
          typename TPosteriorsPrecisionType, typename TPriorsPrecisionType >
void
BayesianClassifierImageFilter< TInputVectorImage, TLabelsType,
                               TPosteriorsPrecisionType, TPriorsPrecisionType >
::ComputeBayesRule()
{
  itkDebugMacro(<< "Computing Bayes Rule");
  const InputImageType *membershipImage = this->GetInput();

  ImageRegionType imageRegion  = membershipImage->GetBufferedRegion();

  if ( m_UserProvidedPriors )
    {
    const PriorsImageType *priorsImage =
      dynamic_cast< const PriorsImageType * >( this->GetInput(1) );

    if ( priorsImage == NULL )
      {
      itkExceptionMacro("Second input type does not correspond to expected Priors Image Type");
      }

    PosteriorsImageType *posteriorsImage =
      dynamic_cast< PosteriorsImageType * >( this->GetPosteriorImage() );

    if ( posteriorsImage == NULL )
      {
      itkExceptionMacro("Second output type does not correspond to expected Posteriors Image Type");
      }

    InputImageIteratorType      itrMembershipImage(membershipImage, imageRegion);
    PriorsImageIteratorType     itrPriorsImage(priorsImage,     imageRegion);
    PosteriorsImageIteratorType itrPosteriorsImage(posteriorsImage, imageRegion);

    itrMembershipImage.GoToBegin();
    itrPriorsImage.GoToBegin();

    const unsigned int numberOfClasses = membershipImage->GetVectorLength();

    itkDebugMacro(<< "Computing Bayes Rule nclasses in membershipImage: " << numberOfClasses);

    while ( !itrMembershipImage.IsAtEnd() )
      {
      PosteriorsPixelType       posteriors(numberOfClasses);
      const PriorsPixelType     priors      = itrPriorsImage.Get();
      const MembershipPixelType memberships = itrMembershipImage.Get();
      for ( unsigned int i = 0; i < numberOfClasses; i++ )
        {
        posteriors[i] =
          static_cast< TPosteriorsPrecisionType >( memberships[i] * priors[i] );
        }
      itrPosteriorsImage.Set(posteriors);
      ++itrMembershipImage;
      ++itrPriorsImage;
      ++itrPosteriorsImage;
      }
    }
  else
    {
    PosteriorsImageType *posteriorsImage =
      dynamic_cast< PosteriorsImageType * >( this->GetPosteriorImage() );

    if ( posteriorsImage == NULL )
      {
      itkExceptionMacro("Second output type does not correspond to expected Posteriors Image Type");
      }

    InputImageIteratorType      itrMembershipImage(membershipImage, imageRegion);
    PosteriorsImageIteratorType itrPosteriorsImage(posteriorsImage, imageRegion);

    itrMembershipImage.GoToBegin();
    itrPosteriorsImage.GoToBegin();

    while ( !itrMembershipImage.IsAtEnd() )
      {
      itrPosteriorsImage.Set( itrMembershipImage.Get() );
      ++itrMembershipImage;
      ++itrPosteriorsImage;
      }
    }
}

template< typename TInputVectorImage, typename TLabelsType,
          typename TPosteriorsPrecisionType, typename TPriorsPrecisionType >
void
BayesianClassifierImageFilter< TInputVectorImage, TLabelsType,
                               TPosteriorsPrecisionType, TPriorsPrecisionType >
::SetSmoothingFilter(SmoothingFilterType *smoothingFilter)
{
  this->m_SmoothingFilter = smoothingFilter;
  this->m_UserProvidedSmoothingFilter = true;
  this->Modified();
}

/**
  * Set the prior image
  */
template< typename TInputVectorImage, typename TLabelsType,
          typename TPosteriorsPrecisionType, typename TPriorsPrecisionType >
void
BayesianClassifierImageFilter< TInputVectorImage, TLabelsType,
                               TPosteriorsPrecisionType, TPriorsPrecisionType >
::SetPriors(const PriorsImageType *priors)
{
  this->ProcessObject::SetNthInput( 1, const_cast< PriorsImageType * >( priors ) );
  this->m_UserProvidedPriors = true;
  this->Modified();
}

/**
 * Normalize the posteriors and smooth them using an user-provided.
 */
template< typename TInputVectorImage, typename TLabelsType,
          typename TPosteriorsPrecisionType, typename TPriorsPrecisionType >
void
BayesianClassifierImageFilter< TInputVectorImage, TLabelsType,
                               TPosteriorsPrecisionType, TPriorsPrecisionType >
::NormalizeAndSmoothPosteriors()
{
  PosteriorsImageIteratorType itrPosteriorImage(
    this->GetPosteriorImage(), this->GetPosteriorImage()->GetBufferedRegion() );

  PosteriorsPixelType p;
  const unsigned int  numberOfClasses = this->GetPosteriorImage()->GetVectorLength();

  for ( unsigned int iter = 0; iter < m_NumberOfSmoothingIterations; iter++ )
    {
    itrPosteriorImage.GoToBegin();
    while ( !itrPosteriorImage.IsAtEnd() )
      {
      p = itrPosteriorImage.Get();

      // Normalize P so the probablity across components sums to 1
      TPosteriorsPrecisionType probability = 0;
      for ( unsigned int i = 0; i < numberOfClasses; i++ )
        {
        probability += p[i];
        }
      p /= probability;
      itrPosteriorImage.Set(p);
      ++itrPosteriorImage;
      }

    for ( unsigned int componentToExtract = 0; componentToExtract < numberOfClasses; componentToExtract++ )
      {
      // Create an auxiliary image to store one component of the vector image.
      // Smoothing filters typically can't handle multi-component images, so we
      // will extract each component and smooth it.
      typename ExtractedComponentImageType::Pointer extractedComponentImage =
        ExtractedComponentImageType::New();
      extractedComponentImage->CopyInformation( this->GetPosteriorImage() );
      extractedComponentImage->SetBufferedRegion(
        this->GetPosteriorImage()->GetBufferedRegion() );
      extractedComponentImage->SetRequestedRegion(
        this->GetPosteriorImage()->GetRequestedRegion() );
      extractedComponentImage->Allocate();
      typedef itk::ImageRegionIterator< ExtractedComponentImageType > IteratorType;

      itrPosteriorImage.GoToBegin();
      IteratorType it( extractedComponentImage,
                       extractedComponentImage->GetBufferedRegion() );

      it.GoToBegin();
      while ( !itrPosteriorImage.IsAtEnd() )
        {
        it.Set(itrPosteriorImage.Get()[componentToExtract]);
        ++it;
        ++itrPosteriorImage;
        }

      m_SmoothingFilter->SetInput(extractedComponentImage);
      m_SmoothingFilter->Modified(); // Force an update
      m_SmoothingFilter->Update();

      itrPosteriorImage.GoToBegin();

      IteratorType sit( m_SmoothingFilter->GetOutput(),
                        m_SmoothingFilter->GetOutput()->GetBufferedRegion() );
      sit.GoToBegin();
      while ( !itrPosteriorImage.IsAtEnd() )
        {
        PosteriorsPixelType posteriorPixel = itrPosteriorImage.Get();
        posteriorPixel[componentToExtract] = sit.Get();
        itrPosteriorImage.Set(posteriorPixel);
        ++sit;
        ++itrPosteriorImage;
        }
      }
    }
}

/**
 * Compute the labeled map based on the Maximum rule applied to the posteriors.
 */
template< typename TInputVectorImage, typename TLabelsType,
          typename TPosteriorsPrecisionType, typename TPriorsPrecisionType >
void
BayesianClassifierImageFilter< TInputVectorImage, TLabelsType,
                               TPosteriorsPrecisionType, TPriorsPrecisionType >
::ClassifyBasedOnPosteriors()
{
  OutputImagePointer labels = this->GetOutput();

  ImageRegionType imageRegion  = labels->GetBufferedRegion();

  PosteriorsImageType *posteriorsImage =
    dynamic_cast< PosteriorsImageType * >( this->GetPosteriorImage() );

  if ( posteriorsImage == NULL )
    {
    itkExceptionMacro("Second output type does not correspond to expected Posteriors Image Type");
    }

  OutputImageIteratorType     itrLabelsImage(labels,         imageRegion);
  PosteriorsImageIteratorType itrPosteriorsImage(posteriorsImage, imageRegion);

  DecisionRulePointer decisionRule = DecisionRuleType::New();

  itrLabelsImage.GoToBegin();
  itrPosteriorsImage.GoToBegin();

  typename PosteriorsImageType::PixelType posteriorsPixel;
  typename DecisionRuleType::MembershipVectorType posteriorsVector;
  posteriorsPixel = itrPosteriorsImage.Get();
  posteriorsVector.reserve( posteriorsPixel.Size() );
  posteriorsVector.insert(posteriorsVector.begin(),
                          posteriorsPixel.Size(), 0.0);
  while ( !itrLabelsImage.IsAtEnd() )
    {
    posteriorsPixel = itrPosteriorsImage.Get();
    std::copy(posteriorsPixel.GetDataPointer(),
              posteriorsPixel.GetDataPointer()+posteriorsPixel.Size(),
              posteriorsVector.begin() );
    itrLabelsImage.Set( static_cast< TLabelsType >(
                          decisionRule->Evaluate( posteriorsVector ) ) );
    ++itrLabelsImage;
    ++itrPosteriorsImage;
    }
}
} // end namespace itk

#endif
