#!/usr/bin/perl
#
# OBTENER TODOS LOS REGISTRADOS: curl -v -k -X GET -H 'Accept: application/json' -H 'authorization: Bearer 1b45dee4-d234-3827-9256-2927e9357c39' 'https://localhost:8243/api/device-mgt/v1.0/devices/1.0.0'
#
#
use strict;
use LWP::UserAgent ();
use JSON;
use Config::Tiny;
use Data::Dumper 'Dumper';

my $ua = LWP::UserAgent->new(
  ssl_opts => {
    verify_hostname => 0,
  },
);
my $config = Config::Tiny->read( "../config/config.ini" );
my $USER = $config->{ WSO2 }->{ USER };
my $PASS = $config->{ WSO2 }->{ PASS };
my $IP = $config->{ WSO2 }->{ IP };
my $token;

# &launch_test();
# print &generate_token()."\n";
# body={
#   METHOD = POST|PUT|GET
#   OPERATION = change-leds|change-time|send-command|device-download
#   ID=string
#   NAME = string
#   ARGS = { ARG1=>VALUE1, ARG2 =>VALUE2, ARG3=>VALUE3.... }
#   EXPECT_ERR = true|false
# }

sub launch_test
{
  my $body = shift;

  my $errmsg = &check_body( $body );
  die "[ERROR] $errmsg->{msg} \n" if ( $errmsg != 0 );

  #Obtain the Access Token for the whole process
  my $access_token = &generate_token();
  my $uri = {
    'change_leds' => "/tempsensor/1.0.0/device/$body->{ID}/change-leds/\?estado=$body->{ARGS}->{estado}",
    'change_time' => "/tempsensor/1.0.0/device/$body->{ID}/change-leds/\?estado=$body->{ARGS}->{tiempo}",
    # 'send-command' => "/device/$body->{ID}/change-leds/\?estado=$body->{ARGS}->{estado}",
    'enroll'      => "/tempsensor/1.0.0/device/download\?deviceName=$body->{NAME}\&sketchType=tempsensor",
    'list'        => "/api/device-mgt/v1.0/devices/1.0.0",
  };
  print "Access token: $access_token\n";
  my $response;
  #Launch the operation;
  if ( $body->{ METHOD } eq "POST")
  {
    # $response = $ua->post ( "http://$IP:8280$uri->{ $body->{ OPERATION } }", "Accept" => "application/json", "authorization" => " Bearer $access_token" )
    my $cmd = "curl --silent -k -X POST -H \'Authorization: Bearer $access_token\' \'https://$IP:8243$uri->{ $body->{ OPERATION } }\'";
    print "CMD: $cmd----\n";
    $response = `curl --silent -k -X POST -H \'Authorization: Bearer $access_token\' \'https://$IP:8243$uri->{ $body->{ OPERATION } }\'`;
    chomp $response;
  }
  elsif ( $body->{ METHOD } eq "GET")
  {
    $response = system "curl --silent -k -X GET -H \'Authorization: Bearer $access_token\' \'https://$IP:8243$uri->{ $body->{ OPERATION } }\' -LO" if ( $body->{ OPERATION} eq "enroll");
    $response = `curl --silent -k -X GET -H \'Authorization: Bearer $access_token\' \'https://$IP:8243$uri->{ $body->{ OPERATION } }\'` if ( $body->{ OPERATION} ne "enroll");
    chomp $response;
  }

  # print Dumper $response;
  if ( exists $body->{ METHOD } )
  {
    $errmsg = &test_resolution( $response, $body->{ expect_err } ) if ( $body->{ OPERATION } ne "enroll" );
    if( $body->{ OPERATION } eq "enroll" )
    {
      $errmsg = $response;
      ok ( $response == 0 );
    }
  }

  if ( exists $body->{ LOCAL } )
  {
    $errmsg = system ( $body->{ LOCAL });
    ok ( $errmsg == 0 );
  }
  return $errmsg;
}

sub test_resolution
{
  my $response = shift;
  my $expect_err = shift // undef;

  use Test::Simple;
  my $output;
  $output = ok( $response->code eq "200") if ( $expect_err eq "false" or ! defined $expect_err );
  $output = ok( $response->code >= 400) if ( $expect_err eq "true" );

  return $output;
}
#Check if the body is correctly built
sub check_body
{
  my $body = shift;
  my $oper = $body->{ OPERATION };
  my $check_hash = {
    method => {
      'change_leds' => "POST",
      'change_time' => "POST",
      "enroll"      => "GET",
    },
  };
  if ( exists $body->{ METHOD} )
  {
    return { msg => "Body is empty" } unless ( defined $body );
    return { msg => "Method $body->{ METHOD } not allowed" }
      unless ( $body->{ METHOD } eq "POST"
        or $body->{ METHOD } eq "PUT"
        or $body->{ METHOD } eq "GET"  );
    return { msg => "Method $body->{ METHOD } not for $body->{ OPERATION } operation" }
      unless ( $check_hash->{method}->{ $oper } eq $body->{ METHOD } );
    return { msg => "Arguments are not right for $body->{OPERATION}" } if ( ( ! exists $body->{ ARGS }->{ tiempo } and $body->{ OPERATION } eq "change-time" ) || ( ! exists $body->{ ARGS }->{ estado } and $body->{ OPERATION } eq "change-leds" ) );
  }
  return 0
}

sub generate_token
{
  my $tkn;

  #Get the encoded user:pass
  my $encoded = &encode( $USER, $PASS );

  #Get the client_id and client_secret
  my $response = `curl --silent -k -X POST https://$IP:8243/api-application-registration/register -H 'authorization: Basic $encoded' -H 'content-type: application/json' -d '{ "applicationName":"tempsensor", "tags":["device_management"]}' 2>/dev/null`;
  chomp $response;

  die "[ERROR] Curl failed obtaining the client secret" if ($?);
  my $clientSecret = decode_json $response;

  my $encodedClient = &encode( $clientSecret->{ client_id }, $clientSecret->{ client_secret } );

  $response = `curl --silent -k -d "grant_type=password&username=$USER&password=$PASS&scope=perm:tempsensor:enroll perm:devices:view perm:devices:delete" -H "Authorization: Basic $encodedClient" -H "Content-Type: application/x-www-form-urlencoded" https://$IP:9443/oauth2/token 2>/dev/null`;
  chomp $response;
  die "[ERROR] Curl failed obtaining the access token" if ($?);

  #Get the token from the decoded access;
  $tkn = decode_json( $response )->{ access_token };

  return $tkn;
}

sub encode
{
  my $user = shift;
  my $pass = shift;
  my $encoded = `echo -n $user:$pass | base64`;
  chomp $encoded;
  return $encoded

}

#TODO: Function to remote check something
#curl 'https://localhost:9443/devicemgt/api/devices/sketch/download?deviceName=manolo&deviceType=tempsensor&sketchType=tempsensor' -H 'Host: localhost:9443' -H 'User-Agent: Mozilla/5.0 (X11; Linux x86_64; rv:60.0) Gecko/20100101 Firefox/60.0' -H 'Accept: text/html,application/xhtml+xml,application/xml;q=0.9,*/*;q=0.8' -H 'Accept-Language: es-ES,es;q=0.8,en-US;q=0.5,en;q=0.3' --compressed -H 'Referer: https://localhost:9443/devicemgt/device/tempsensor/enroll' -H 'Cookie: JSESSIONID=6513E784771D5D2475A2442BB9C78CD5; JSESSIONID=A7D2B38F0C5340891146403DE73CE5FF; commonAuthId=7b6f4063-bf5e-4ffa-bd04-d6f5782ea967; samlssoTokenId=75bf7786-9e02-44ef-9583-4bd0ba89fefe; csrftoken=hqh2c979lgqneci98ac4sg9rgu; i18next=es-ES' -H 'Connection: keep-alive' -H 'Upgrade-Insecure-Requests: 1'
1;
