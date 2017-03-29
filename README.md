# Slack Appender for Log4j

This is a simple webhook-based Slack appender for Log4j 2.x.
To use this appender in your code, first get a webhook URL from [your Slack apps](https://api.slack.com/apps).
Then, add this to your project's dependencies:

```
dependencies {
    compile 'com.uptake.log4j:log4j-slack:1.0'
}
```

Currently, this only requires Log4j and Jackson Core, but in a future release of Log4j 2, the Jackson Core dependency will no longer be required.
To use this plugin, add the following to your `log4j2.xml` configuration (or translate to your preferred format):

```
<Configuration>
    <Appenders>
        <Slack name="slack" webhook="https://hooks.slack.com/services/...">
            <MarkerFilter marker="SLACK"/>
            <PatternLayout pattern="%m"/>
        </Slack>
    </Appenders>
    <Loggers>
        <Root level="info">
            <AppenderRef ref="slack"/>
        </Root>
    </Loggers>
</Configuration>
```

In a more realistic configuration, you simply need to add a `<Slack/>` element in your `<Appenders/>` block.
I recommend adding a [MarkerFilter](https://logging.apache.org/log4j/2.x/manual/filters.html#MarkerFilter) to the appender so that you can use code like the following:

```
@Log4j2
public class Example {
    private static final Marker SLACK = MarkerManager.getMarker("SLACK");
    public void run() {
        log.error(SLACK, "Hey team, something is wrong with our service!");
    }
}
```

```
@Slf4j
public class Example {
    private static final Marker SLACK = MarkerFactory.getMarker("SLACK");
    public void run() {
        log.error(SLACK, "Something is wrong with our service besides SLF4J!");
    }
}
```

Using the MarkerFilter configuration like this allows you to send a log message to Slack from any class.
By using the `SLACK` marker, this allows the configuration to filter all messages with that marker and make sure to send them to the SlackAppender.
As long as you don't override the additivity property of the relevant loggers, then these log messages will also be logged to the console or file or whatever you have configured normally.
Read more about markers [here](https://logging.apache.org/log4j/2.x/manual/markers.html).

The following are all the configuration attributes or elements supported:

* name: name of the appender, used when referencing via `<AppenderRef ref="name"/>`.
* filter: an optional filter to use; a MarkerFilter is recommended here as described above.
* ignoreExceptions: whether or not to let exceptions from the appender to be swallowed or propagated.
  This is true by default and should usually only be set to false when using a [FailoverAppender](https://logging.apache.org/log4j/2.x/manual/appenders.html#FailoverAppender) or similar.
* layout: a string layout to use to format log events into a Slack message.
  Only `StringLayout` layouts are supported since sending arbitrary binary data to Slack doesn't make sense.
  If no layout is specified, the default pattern layout is used.
* webhook: URL to the Slack webhook to send messages to.
