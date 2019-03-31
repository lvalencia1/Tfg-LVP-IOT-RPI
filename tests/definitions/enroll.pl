#!/usr/bin/perl
use Config::Tiny;
my $config = Config::Tiny->read("../config/config.ini");

my $AGENT_IP = $config->{RPI}->{IP};

sub new_device
{
  my $NAME = shift // "TEST1";
  my $json = {
    METHOD => "GET",
    OPERATION => "enroll",
    NAME => $NAME,
  };
  return $json;
}

sub upload_agent
{

  my $json = {
    LOCAL => "scp ./download* pi\@$AGENT_IP:agent.zip >/dev/null 2>&1"
  };
  return $json;
}

sub unzip_agent
{

  my $json = {
    LOCAL => "ssh pi\@$AGENT_IP 'cd agent; rm -rf ./* > /dev/null 2>&1; unzip ../agent.zip > /dev/null 2>&1'"
  };
  return $json;
}

1;
