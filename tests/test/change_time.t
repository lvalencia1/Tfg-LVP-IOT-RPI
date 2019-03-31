#!/usr/bin/perl
use Config::Tiny;
my $config = Config::Tiny->read("../config/config.ini");

my $AGENT_IP = $config->{RPI}->{IP};

sub change_time
{
  my $test_def;
  my $errors = 0;

  $test_def = &set_time("4");
  $errors += &launch_test($test_def);

  sleep 5;
  $test_def = &check_time ("4");
  $errors += &launch_test($test_def);

  $test_def = &set_time("10");
  $errors += &launch_test($test_def);

  sleep 5;
  $test_def = &check_time ("10");
  $errors += &launch_test($test_def);

}
1;
