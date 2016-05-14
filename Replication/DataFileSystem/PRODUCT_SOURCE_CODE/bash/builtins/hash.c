This file is hash.def, from which is created hash.c.
It implements the builtin "hash" in Bash.

Copyright (C) 1987-2010 Free Software Foundation, Inc.

This file is part of GNU Bash, the Bourne Again SHell.

Bash is free software: you can redistribute it and/or modify
it under the terms of the GNU General Public License as published by
the Free Software Foundation, either version 3 of the License, or
(at your option) any later version.

Bash is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
GNU General Public License for more details.

You should have received a copy of the GNU General Public License
along with Bash.  If not, see <http://www.gnu.org/licenses/>.

$PRODUCES hash.c

$BUILTIN hash
$FUNCTION hash_builtin
$SHORT_DOC hash [-lr] [-p pathname] [-dt] [name ...]
Remember or display program locations.

Determine and remember the full pathname of each command NAME.  If
no arguments are given, information about remembered commands is displayed.

Options:
  -d		forget the remembered location of each NAME
  -l		display in a format that may be reused as input
  -p pathname	use PATHNAME is the full pathname of NAME
  -r		forget all remembered locations
  -t		print the remembered location of each NAME, preceding
		each location with the corresponding NAME if multiple
		NAMEs are given
Arguments:
  NAME		Each NAME is searched for in $PATH and added to the list
		of remembered commands.

Exit Status:
Returns success unless NAME is not found or an invalid option is given.
$END

#include <config.h>

#include <stdio.h>

#include "../bashtypes.h"

#if defined (HAVE_UNISTD_H)
#  include <unistd.h>
#endif

#include <errno.h>

#include "../bashansi.h"
#include "../bashintl.h"

#include "../shell.h"
#include "../builtins.h"
#include "../flags.h"
#include "../findcmd.h"
#include "../hashcmd.h"
#include "common.h"
#include "bashgetopt.h"

extern int posixly_correct;
extern int dot_found_in_search;
extern char *this_command_name;

static int add_hashed_command __P((char *, int));
static int print_hash_info __P((BUCKET_CONTENTS *));
static int print_portable_hash_info __P((BUCKET_CONTENTS *));
static int print_hashed_commands __P((int));
static int list_hashed_filename_targets __P((WORD_LIST *, int));

/* Print statistics on the current state of hashed commands.  If LIST is
   not empty, then rehash (or hash in the first place) the specified
   commands. */
int
hash_builtin (list)
     WORD_LIST *list;
{
  int expunge_hash_table, list_targets, list_portably, delete, opt;
  char *w, *pathname;

  if (hashing_enabled == 0)
    {
      builtin_error (_("hashing disabled"));
      return (EXECUTION_FAILURE);
    }

  expunge_hash_table = list_targets = list_portably = delete = 0;
  pathname = (char *)NULL;
  reset_internal_getopt ();
  while ((opt = internal_getopt (list, "dlp:rt")) != -1)
    {
      switch (opt)
	{
	case 'd':
	  delete = 1;
	  break;
	case 'l':
	  list_portably = 1;
	  break;
	case 'p':
	  pathname = list_optarg;
	  break;
	case 'r':
	  expunge_hash_table = 1;
	  break;
	case 't':
	  list_targets = 1;
	  break;
	default:
	  builtin_usage ();
	  return (EX_USAGE);
	}
    }
  list = loptend;

  /* hash -t requires at least one argument. */
  if (list == 0 && list_targets)
    {
      sh_needarg ("-t");
      return (EXECUTION_FAILURE);
    }

  /* We want hash -r to be silent, but hash -- to print hashing info, so
     we test expunge_hash_table. */
  if (list == 0 && expunge_hash_table == 0)
    {
      opt = print_hashed_commands (list_portably);
      if (opt == 0 && posixly_correct == 0)
	printf (_("%s: hash table empty\n"), this_command_name);

      return (EXECUTION_SUCCESS);
    }

  if (expunge_hash_table)
    phash_flush ();

  /* If someone runs `hash -r -t xyz' he will be disappointed. */
  if (list_targets)
    return (list_hashed_filename_targets (list, list_portably));
      
#if defined (RESTRICTED_SHELL)
  if (restricted && pathname && strchr (pathname, '/'))
    {
      sh_restricted (pathname);
      return (EXECUTION_FAILURE);
    }
#endif

  for (opt = EXECUTION_SUCCESS; list; list = list->next)
    {
      /* Add, remove or rehash the specified commands. */
      w = list->word->word;
      if (absolute_program (w))
	continue;
      else if (pathname)
	{
	  if (is_directory (pathname))
	    {
#ifdef EISDIR
	      builtin_error ("%s: %s", pathname, strerror (EISDIR));
#else
	      builtin_error (_("%s: is a directory"), pathname);
#endif
	      opt = EXECUTION_FAILURE;
	    }
	  else
	    phash_insert (w, pathname, 0, 0);
	}
      else if (delete)
	{
	  if (phash_remove (w))
	    {
	      sh_notfound (w);
	      opt = EXECUTION_FAILURE;
	    }
	}
      else if (add_hashed_command (w, 0))
	opt = EXECUTION_FAILURE;
    }

  fflush (stdout);
  return (opt);
}

static int
add_hashed_command (w, quiet)
     char *w;
     int quiet;
{
  int rv;
  char *full_path;

  rv = 0;
  if (find_function (w) == 0 && find_shell_builtin (w) == 0)
    {
      phash_remove (w);
      full_path = find_user_command (w);
      if (full_path && executable_file (full_path))
	phash_insert (w, full_path, dot_found_in_search, 0);
      else
	{
	  if (quiet == 0)
	    sh_notfound (w);
	  rv++;
	}
      FREE (full_path);
    }
  return (rv);
}

/* Print information about current hashed info. */
static int
print_hash_info (item)
     BUCKET_CONTENTS *item;
{
  printf ("%4d\t%s\n", item->times_found, pathdata(item)->path);
  return 0;
}

static int
print_portable_hash_info (item)
     BUCKET_CONTENTS *item;
{
  printf ("builtin hash -p %s %s\n", pathdata(item)->path, item->key);
  return 0;
}

static int
print_hashed_commands (fmt)
     int fmt;
{
  if (hashed_filenames == 0 || HASH_ENTRIES (hashed_filenames) == 0)
    return (0);

  if (fmt == 0)
    printf (_("hits\tcommand\n"));
  hash_walk (hashed_filenames, fmt ? print_portable_hash_info : print_hash_info);
  return (1);
}

static int
list_hashed_filename_targets (list, fmt)
     WORD_LIST *list;
     int fmt;
{
  int all_found, multiple;
  char *target;
  WORD_LIST *l;

  all_found = 1;
  multiple = list->next != 0;

  for (l = list; l; l = l->next)
    {
      target = phash_search (l->word->word);
      if (target == 0)
	{
	  all_found = 0;
	  sh_notfound (l->word->word);
	  continue;
	}
      if (fmt)
	printf ("builtin hash -p %s %s\n", target, l->word->word);
      else
	{
	  if (multiple)
	    printf ("%s\t", l->word->word);
	  printf ("%s\n", target);
	}
    }

  return (all_found ? EXECUTION_SUCCESS : EXECUTION_FAILURE);
}
