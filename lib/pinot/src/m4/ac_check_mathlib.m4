dnl @synopsis AC_CHECK_MATHLIB
dnl
dnl This check will check for the math.h include file and see if the
dnl math library -lm needs to be added to the compiler LIBS.
dnl
dnl @version $Id: ac_check_mathlib.m4,v 1.1.1.1 2005/07/17 23:22:39 shini Exp $
dnl @author Mo DeJong <mdejong@cygnus.com>
dnl
AC_DEFUN([AC_CHECK_MATHLIB],
[
AC_CHECK_HEADERS(math.h)
AC_SEARCH_LIBS(cos, m)
]
)

