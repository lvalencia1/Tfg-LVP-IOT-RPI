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
  my $device_ID = `ssh pi\@$AGENT_IP cat agent/deviceConfig.properties | grep deviceId | sed 's/deviceId=//g'`;
  $config->{ RPI }->{ deviceId } = $device_ID;
  use Data::Dumper;
  print Dumper $config;
  $config->write("../config/config.inis");

  #GET de los dispositivos

  return $err;
}
1;
