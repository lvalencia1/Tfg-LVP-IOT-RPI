#!/usr/bin/perl
use Config::Tiny;
my $config = Config::Tiny->read("../config/config.ini");

my $AGENT_IP = $config->{ RPI }->{IP };
my $AGENT_ID = $config->{ RPI }->{ deviceId };

sub set_on
{
  my $json = {
    METHOD => "POST",
    OPERATION => "change_leds",
    ID => $AGENT_ID,
    ARGS => {
      estado => "on"
    }
  };
  return $json
}

sub check_status
{
  my $status = shift;
  my $json = {
    LOCAL => "ssh root\@$AGENT_IP cat agent/deviceConfig.properties | grep leds-state | grep $status >/dev/null"
  };
  return $json
}

sub set_off
{
  my $json = {
    METHOD => "POST",
    OPERATION => "change_leds",
    ID => $AGENT_ID,
    ARGS => {
      estado => "off"
    }
  };
  return $json
}
