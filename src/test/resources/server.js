/**
 * Module dependencies
 */

var Sails = require('sails/lib/app');
var _ = require('lodash');
var fs = require('fs');

// Use a weird port to avoid tests failing if we
// forget to shut down another Sails app
var TEST_SERVER_PORT = process.env.PORT || 1577;
var EXPECTED_RESPONSES = JSON.parse(fs.readFileSync('src/test/resources/expectedResponses.json', 'utf-8'));

/**
 * @type {Object}
 */
module.exports = {

  setup: function (cb) {
    if (typeof cb != 'function') {
      throw new Error('Callback missing!');
    }

    // New up an instance of Sails
    // and lift it.
    var app = Sails();
    app.lift({
      log: { level: 'error' },
      port: TEST_SERVER_PORT,
      sockets: {
        authorization: false
      },
      hooks: {
        grunt: false
      },
      routes: {
        '/greeting': function(req, res) {
          res.ok("Hello world!");
        }
      }
    },function (err) {
      if (err) return cb(err);

      // Globalize sails app as `server`
      global.server = app;
      setupRoutes(EXPECTED_RESPONSES);
      return cb(err);
    });

  },

  teardown: function () {

    // Tear down sails server
    global.server.lower();
  }
};

// Bind routes which respond with the expected data.
function setupRoutes(expectedResponses) {
    _.each(expectedResponses, function (expectedResponse, routeAddress) {
        server.router.bind(routeAddress, function (req, res) {
            return res.send(expectedResponse.statusCode || 200, expectedResponse.req && _dotToObject(req, expectedResponse.req) || expectedResponse.body);
        });
    });
}

//Helper function we use to do a lookup on an object.
//It splits the dot string to an object path.
function _dotToObject(obj, path) {
    return path.split('.').reduce(function objectIndex(obj, i) {
        return obj[i];
    }, obj);
}