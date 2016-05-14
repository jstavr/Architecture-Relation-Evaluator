// This is core/vnl/vnl_matlab_filewrite.h
#ifndef vnl_matlab_filewrite_h_
#define vnl_matlab_filewrite_h_
#ifdef VCL_NEEDS_PRAGMA_INTERFACE
#pragma interface
#endif
//:
//  \file
//  \author David Capel, Oxford RRG
//  \date   17 August 1998
//
// \verbatim
// Modifications
// LSB (Manchester) 23/3/01  Tidied documentation
//   Feb.2002 - Peter Vanroose - brief doxygen comment placed on single line
// \endverbatim

#include <vcl_string.h>
#include <vcl_fstream.h>
#include <vcl_complex.h>

#include <vnl/vnl_vector.h>
#include <vnl/vnl_matrix.h>

//: Code to perform MATLAB binary file operations
//    vnl_matlab_filewrite is a collection of I/O functions for reading/writing
//    matrices in the compact MATLAB binary format (.mat)

class vnl_matlab_filewrite
{
 public:
  vnl_matlab_filewrite (char const* file_name, char const *basename = 0);

  //: Add scalar/vector/matrix variable to the MAT file using specified variable name.
  // If no name is given, variables will be generated by
  // appending 0,1,2 etc to the given basename.
  void write(double v, char const* variable_name = 0);

  void write(vnl_vector<double> const & v, char const* variable_name = 0);
  void write(vnl_vector<vcl_complex<double> > const & v, char const* variable_name = 0);

  void write(vnl_matrix<float> const & M, char const* variable_name = 0);
  void write(vnl_matrix<double> const & M, char const* variable_name = 0);
  void write(vnl_matrix<vcl_complex<float> > const & M, char const* variable_name = 0);
  void write(vnl_matrix<vcl_complex<double> > const & M, char const* variable_name = 0);

  void write(double const * const *M, int rows, int cols, char const* variable_name = 0);

 protected:
  vcl_string basename_;
  int variable_int_;
  vcl_fstream out_;

  vcl_string make_var_name(char const* variable_name);
};

#endif // vnl_matlab_filewrite_h_
