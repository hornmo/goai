package com.k_int.goai

import groovyx.net.http.*
import static groovyx.net.http.ContentType.*
import static groovyx.net.http.Method.*

public class OaiClient {

  String host

  public OaiClient() {
  }

  public getChangesSince(datestamp, metadataPrefix, processing_closure) {
    println("Get latest changes");

    def http = new HTTPBuilder( host )

    def more = true
    println("Attempt get...");
    def resumption=null

    // perform a GET request, expecting JSON response data
    while ( more ) {
      println("Make http request..");

      http.request( GET, XML ) {
        uri.path = '/gokb/oai/packages'
        if ( resumption ) {
          uri.query = [ verb:'ListRecords', resumptionToken: resumption ]
        }
        else {
          uri.query = [ verb:'ListRecords', metadataPrefix: metadataPrefix ]
        }
  
        // response handler for a success response code:
        response.success = { resp, xml ->
          println resp.statusLine
  
          xml.'ListRecords'.'record'.each { r ->
            // println("Record id...${r.'header'.'identifier'}");
            // println("Package Name: ${r.metadata.package.packageName}");
            // println("Package Id: ${r.metadata.package.packageId}");
            processing_closure.call(r);
          }

          if ( xml.'ListRecords'.'resumptionToken'.size() == 1 ) {
            resumption=xml.'ListRecords'.'resumptionToken'.text()
            println("Iterate with resumption : ${resumption}");
          }
          else {
            more = false
          }
        }

        // handler for any failure status code:
        response.failure = { resp ->
          println "Unexpected error: ${resp.statusLine.statusCode} : ${resp.statusLine.reasonPhrase}"
        }
      }
      println("Endloop");
    }

    println("All done");
  }

}
