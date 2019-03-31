#!/usr/bin/perl
use Config::Tiny;
my $config = Config::Tiny->read("../config/config.ini");

my $AGENT_IP = $config->{RPI}->{IP};

sub change_leds
{
  my $test_def;
  my $errors = 0;

  $test_def = &set_on();
  $errors += &launch_test($test_def);

  sleep 5;

  $test_def = &check_status("on");
  $errors += &launch_test($test_def);

  sleep 5;

  $test_def = &set_off();
  $errors += &launch_test($test_def);

  sleep 5;

  $test_def = &check_status("off");
  $errors += &launch_test($test_def);

}
1;
