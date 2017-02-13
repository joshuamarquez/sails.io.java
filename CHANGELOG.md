# Change Log

### v0.4.0

 _2017-02-12_ [v0.3.0...v0.4.0](https://github.com/joshuamarquez/sails.io.java/compare/v0.3.0...v0.4.0)

* [BUG FIXED] `SailsIOClient` is no longer a singleton. This decision was taken due to an inconsistency found when multiple libraries dependent this library were used in one app (global socket was the same in all libs). [9d36385...f83cd52](https://github.com/joshuamarquez/sails.io.java/compare/9d36385...f83cd52).
