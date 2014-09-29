## onyx-core-async

Onyx plugin providing read and write facilities for Clojure core.async. This plugin should only be used with in-memory mode for tests and is not suitable for a production environment.

#### Installation

In your project file:

```clojure
[com.mdrogalis/onyx-core-async "0.3.2"]
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

#### Contributing

Pull requests into the master branch are welcomed.

#### License

Copyright © 2014 Michael Drogalis

Distributed under the Eclipse Public License, the same as Clojure.
