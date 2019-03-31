#!/usr/bin/perl
use Config::Tiny;
my $config = Config::Tiny->read("../config/config.ini");

my $AGENT_IP = $config->{RPI}->{IP};

sub enroll_device
{
  my $NAME = "TEST1";


  my $err = 0;
  my $test_def;

  $test_def = &new_device( $NAME );
  $err += &launch_test( $test_def );

  #Pasar a la raspberry
  $test_def = &upload_agent();
  $err += &launch_test( $test_def );

  #Descomprimir
  $test_def = &unzip_agent();
  $err += &launch_test( $test_def );

  #Iniciar
  system "ssh root\@$AGENT_IP 'chmod +x agent/*.sh'";
  my $device_ID = `ssh root\@$AGENT_IP cat agent/deviceConfig.properties | grep deviceId | sed 's/deviceId=//g'`;
  chomp $device_ID;

  $config->{ RPI }->{ deviceId } = $device_ID;
  $config->write("../config/config.ini");

  system "ssh root\@$AGENT_IP 'cd agent; ./agentScript.sh'";

  #GET de los dispositivos

  return $err;
}
1;
