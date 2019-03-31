#!/usr/bin/perl
use Config::Tiny;
my $config = Config::Tiny->read("../config/config.ini");

my $AGENT_IP = $config->{ RPI }->{IP };
my $AGENT_ID = $config->{ RPI }->{ deviceId };

sub set_time
{
  my $tiempo = shift;
  my $json = {
    METHOD => "POST",
    OPERATION => "change_time",
    ID => $AGENT_ID,
    ARGS => {
      tiempo => "$tiempo"
    }
  };
  return $json
}

sub check_time
{
  my $time = shift;
  my $json = {
    LOCAL => "ssh root\@$AGENT_IP cat agent/deviceConfig.properties | grep push-interval | grep $time >/dev/null"
  };
  return $json
}
