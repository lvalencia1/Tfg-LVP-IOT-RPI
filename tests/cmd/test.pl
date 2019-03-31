#!/usr/bin/perl

my $perl_test = shift;
use Cwd qw(cwd);

my $dir = cwd();

&load_def( "$dir/../definitions" );
#Load core
require "$dir/core.pl";

die "Not test not found" unless ( -f "$dir/../test/$perl_test.t" );

require "$dir/../test/$perl_test.t";

my $err = &$perl_test();
use Test::More;
done_testing();
use POSIX;

POSIX::exit $err;

sub load_def
{
  my $dir = shift;
  my @files = split " ", `ls $dir`;
  foreach $file ( @files )
  {
    require "$dir/$file";
    print "$dir/$file \n";
  }
}
