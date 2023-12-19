# RSocket reconnect issue

In rsocket-core 1.1.4 there is a change (likely [this one](http://bla.com)) which causes 
different behavior when connection issues occur. 

`NoKeepAliveAckReconnectTest.java` demonstrates this behaviour working as expected when using
`io.rsocket:rsocket-core:1.1.3`. However when switching to `io.rsocket:rsocket-core:1.1.4` the test fails because the
error is emitted on `requestor.retrieveMono(..)`.

The test is using `Thread.sleep` directly on the resulting thread from the requestor to simulate the keep alive failure.
