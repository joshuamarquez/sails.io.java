var sailsServer = require('./server');

sailsServer.setup(function(err) {
    if (err) {
        throw err;
    }

    server.router.bind("/count", function (req, res) {
        var count;
        if (!req.session) {
            return res.send("NO_SESSION");
        }
        count = req.session.count || 1;
        req.session.count = count + 1;
        return res.send(200, count);
    });

    console.log('Sails server running.');
});

process.on('exit', function() {
    console.log('Shutting down Sails server.');

    sailsServer.teardown();
});