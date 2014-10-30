## onyx-core-async

Onyx plugin providing read and write facilities for Clojure core.async.

#### Installation

In your project file:

```clojure
[com.mdrogalis/onyx-core-async "0.4.0"]
```

In your peer boot-up namespace:

```clojure
(:require [onyx.plugin.core-async])
```

#### Catalog entries

##### read-from-chan

```clojure
{:onyx/name :input
 :onyx/ident :core.async/read-from-chan
 :onyx/type :input
 :onyx/medium :core.async
 :onyx/consumption :sequential
 :onyx/batch-size batch-size
 :onyx/doc "Reads segments from a core.async channel"}
```

##### write-to-chan

```clojure
{:onyx/name :output
 :onyx/ident :core.async/write-to-chan
 :onyx/type :output
 :onyx/medium :core.async
 :onyx/consumption :sequential
 :onyx/batch-size batch-size
 :onyx/doc "Writes segments to a core.async channel"}
```

#### Attributes

This plugin does not use any attributes.

#### Lifecycle Arguments

References to core.async channels must be injected for both the input and output tasks.

##### `read-from-chan`

```clojure
(defmethod l-ext/inject-lifecycle-resources :my.input.task.identity
  [_ _] {:core-async/in-chan (chan capacity})
```

##### `write-to-chan`

```clojure
(defmethod l-ext/inject-lifecycle-resources :my.output.task.identity
  [_ _] {:core-async/out-chan (chan capacity)})
```

#### Functions

##### `take-segments!`

This additional function is provided as a utility for removing segments
from a channel until `:done` is found. After `:done` is encountered, all prior segments,
including `:done`, are returned in a seq.

#### Contributing

Pull requests into the master branch are welcomed.

#### License

Copyright © 2014 Michael Drogalis

Distributed under the Eclipse Public License, the same as Clojure.

