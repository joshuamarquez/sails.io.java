var sailsServer = require('./server');

sailsServer.setup(function(err) {
    if (err) {
        throw err;
    }

    console.log('Sails server running.');
});

process.on('exit', function() {
    console.log('Shutting down Sails server.');

    sailsServer.teardown();
});