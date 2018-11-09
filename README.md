# gcs-database-history [![CircleCI](https://circleci.com/gh/hden/gcs-database-history/tree/master.svg?style=svg)](https://circleci.com/gh/hden/gcs-database-history/tree/master) [![codecov](https://codecov.io/gh/hden/gcs-database-history/branch/master/graph/badge.svg)](https://codecov.io/gh/hden/gcs-database-history)
[![FOSSA Status](https://app.fossa.io/api/projects/git%2Bgithub.com%2Fhden%2Fgcs-database-history.svg?type=shield)](https://app.fossa.io/projects/git%2Bgithub.com%2Fhden%2Fgcs-database-history?ref=badge_shield)

A [debezium](https://github.com/debezium/debezium) DatabaseHistory implementation that stores the schema history in a GCS bucket.

## Usage

```connector.properties
database.history=io.debezium.relational.history.gcs.GCSDatabaseHistory
database.history.gcs.bucket=bucket
database.history.gcs.prefix=path/prefix-
```

## License

Copyright © 2018 Haokang Den <haokang.den@gmail.com>

Distributed under the Eclipse Public License either version 1.0 or (at
your option) any later version.


[![FOSSA Status](https://app.fossa.io/api/projects/git%2Bgithub.com%2Fhden%2Fgcs-database-history.svg?type=large)](https://app.fossa.io/projects/git%2Bgithub.com%2Fhden%2Fgcs-database-history?ref=badge_large)