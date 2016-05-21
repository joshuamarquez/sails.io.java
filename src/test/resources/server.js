/**
 * Module dependencies
 */

var Sails = require('sails/lib/app');

// Use a weird port to avoid tests failing if we
// forget to shut down another Sails app
var TEST_SERVER_PORT = 1577;

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
          res.header("Content-type","application/javascript");
          res.ok("Hello world!");
        }
      }
    },function (err) {
      if (err) return cb(err);

      // Globalize sails app as `server`
      global.server = app;
      return cb(err);
    });

  },

  teardown: function (done) {

    // Tear down sails server
    global.server.lower(function () {

     // Delete globals (just in case-- shouldn't matter)
     delete global.server;
     return setTimeout(done, 100);
    });
  }
};