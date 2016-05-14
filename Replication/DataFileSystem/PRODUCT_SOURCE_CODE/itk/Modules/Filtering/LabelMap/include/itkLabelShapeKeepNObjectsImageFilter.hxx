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
#ifndef __itkLabelShapeKeepNObjectsImageFilter_hxx
#define __itkLabelShapeKeepNObjectsImageFilter_hxx

#include "itkLabelShapeKeepNObjectsImageFilter.h"
#include "itkProgressAccumulator.h"

namespace itk
{
template< typename TInputImage >
LabelShapeKeepNObjectsImageFilter< TInputImage >
::LabelShapeKeepNObjectsImageFilter()
{
  m_BackgroundValue = NumericTraits< OutputImagePixelType >::NonpositiveMin();
  m_NumberOfObjects = 1;
  m_ReverseOrdering = false;
  m_Attribute = LabelObjectType::NUMBER_OF_PIXELS;
}

template< typename TInputImage >
void
LabelShapeKeepNObjectsImageFilter< TInputImage >
::GenerateInputRequestedRegion()
{
  // call the superclass' implementation of this method
  Superclass::GenerateInputRequestedRegion();

  // We need all the input.
  InputImagePointer input = const_cast< InputImageType * >( this->GetInput() );
  if ( input )
    {
    input->SetRequestedRegion( input->GetLargestPossibleRegion() );
    }
}

template< typename TInputImage >
void
LabelShapeKeepNObjectsImageFilter< TInputImage >
::EnlargeOutputRequestedRegion(DataObject *)
{
  this->GetOutput()
  ->SetRequestedRegion( this->GetOutput()->GetLargestPossibleRegion() );
}

template< typename TInputImage >
void
LabelShapeKeepNObjectsImageFilter< TInputImage >
::GenerateData()
{
  // Create a process accumulator for tracking the progress of this minipipeline
  ProgressAccumulator::Pointer progress = ProgressAccumulator::New();

  progress->SetMiniPipelineFilter(this);

  // Allocate the output
  this->AllocateOutputs();

  typename LabelizerType::Pointer labelizer = LabelizerType::New();
  labelizer->SetInput( this->GetInput() );
  labelizer->SetBackgroundValue(m_BackgroundValue);
  labelizer->SetNumberOfThreads( this->GetNumberOfThreads() );
  progress->RegisterInternalFilter(labelizer, .3f);

  typename LabelObjectValuatorType::Pointer valuator = LabelObjectValuatorType::New();
  valuator->SetInput( labelizer->GetOutput() );
  valuator->SetLabelImage( this->GetInput() );
  valuator->SetNumberOfThreads( this->GetNumberOfThreads() );
  if ( m_Attribute != LabelObjectType::PERIMETER && m_Attribute != LabelObjectType::ROUNDNESS )
    {
    valuator->SetComputePerimeter(false);
    }
  if ( m_Attribute == LabelObjectType::FERET_DIAMETER )
    {
    valuator->SetComputeFeretDiameter(true);
    }
  progress->RegisterInternalFilter(valuator, .3f);

  typename KeepNObjectsType::Pointer opening = KeepNObjectsType::New();
  opening->SetInput( valuator->GetOutput() );
  opening->SetNumberOfObjects(m_NumberOfObjects);
  opening->SetReverseOrdering(m_ReverseOrdering);
  opening->SetAttribute(m_Attribute);
  opening->SetNumberOfThreads( this->GetNumberOfThreads() );
  progress->RegisterInternalFilter(opening, .2f);

  typename BinarizerType::Pointer binarizer = BinarizerType::New();
  binarizer->SetInput( opening->GetOutput() );
  binarizer->SetNumberOfThreads( this->GetNumberOfThreads() );
  progress->RegisterInternalFilter(binarizer, .2f);

  binarizer->GraftOutput( this->GetOutput() );
  binarizer->Update();
  this->GraftOutput( binarizer->GetOutput() );
}

template< typename TInputImage >
void
LabelShapeKeepNObjectsImageFilter< TInputImage >
::PrintSelf(std::ostream & os, Indent indent) const
{
  Superclass::PrintSelf(os, indent);

  os << indent << "BackgroundValue: "
     << static_cast< typename NumericTraits< OutputImagePixelType >::PrintType >( m_BackgroundValue ) << std::endl;
  os << indent << "NumberOfObjects: "  << m_NumberOfObjects << std::endl;
  os << indent << "ReverseOrdering: "  << m_ReverseOrdering << std::endl;
  os << indent << "Attribute: "  << LabelObjectType::GetNameFromAttribute(m_Attribute) << " (" << m_Attribute << ")"
     << std::endl;
}
} // end namespace itk
#endif
