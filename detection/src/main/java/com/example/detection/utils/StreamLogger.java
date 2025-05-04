package com.example.detection.utils;

import org.apache.flink.api.common.functions.OpenContext;
import org.apache.flink.api.common.functions.RichMapFunction;
import org.apache.flink.streaming.api.datastream.DataStream;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.example.detection.model.Alert;
import com.example.detection.model.Transaction;

/**
 * Utility class for logging data streams in Flink applications.
 * Provides methods to add logging to input and output of data streams.
 */
public class StreamLogger {
    private StreamLogger() {
        // Private constructor to prevent instantiation
    }

    /**
     * Adds logging to a data stream.
     *
     * @param <T>        The type of elements in the data stream
     * @param stream     The data stream to add logging to
     * @param streamName The name of the stream for logging purposes
     * @return The data stream with logging added
     */
    public static <T> DataStream<T> logStream(DataStream<T> stream, String streamName) {
        return stream.map(new RichMapFunction<T, T>() {
            private static final long serialVersionUID = 1L;
            private transient Logger logger;

            @Override
            public void open(OpenContext openContext) {
                logger = LogManager.getLogger(streamName);
            }

            @Override
            public T map(T value) throws Exception {
                logger.info("Stream '{}' received: {}", streamName, value);
                return value;
            }
        }).name("Logger-" + streamName);
    }

    /**
     * Adds logging to a data stream with custom log message.
     *
     * @param <T>        The type of elements in the data stream
     * @param stream     The data stream to add logging to
     * @param streamName The name of the stream for logging purposes
     * @param logMessage The custom log message to use
     * @return The data stream with logging added
     */
    public static <T> DataStream<T> logStream(DataStream<T> stream, String streamName, String logMessage) {
        return stream.map(new RichMapFunction<T, T>() {
            private static final long serialVersionUID = 1L;
            private transient Logger logger;

            @Override
            public void open(OpenContext openContext) {
                logger = LogManager.getLogger(streamName);
            }

            @Override
            public T map(T value) throws Exception {
                logger.info("{}: {}", logMessage, value);
                return value;
            }
        }).name("Logger-" + streamName);
    }

    /**
     * Adds detailed logging specifically for Transaction events.
     *
     * @param stream The transaction data stream to add logging to
     * @return The data stream with detailed transaction logging added
     */
    public static DataStream<Transaction> logTransactionStream(DataStream<Transaction> stream) {
        return stream.map(new RichMapFunction<Transaction, Transaction>() {
            private static final long serialVersionUID = 1L;
            private transient Logger logger;

            @Override
            public void open(OpenContext openContext) {
                logger = LogManager.getLogger("TransactionLogger");
            }

            @Override
            public Transaction map(Transaction transaction) throws Exception {
                logger.info("Transaction received - Key: {}, EventTime: {}, IngestTime: {}",
                        transaction.getKey(),
                        transaction.getEventTimestamp(),
                        transaction.getIngestTimestamp());
                return transaction;
            }
        }).name("Transaction-Logger");
    }

    public static DataStream<Alert> logAlertStream(DataStream<Alert> stream) {
        return stream.map(new RichMapFunction<Alert, Alert>() {
            private static final long serialVersionUID = 1L;
            private transient Logger logger;

            @Override
            public void open(OpenContext openContext) {
                logger = LogManager.getLogger("AlertLogger");
            }

            @Override
            public Alert map(Alert alert) throws Exception {
                logger.info("Alert received - Events: {}, RuleID: {}, Timestamp: {}",
                        alert.getEvents(),
                        alert.getRule().getId(),
                        alert.getIngestTimestamp());
                return alert;
            }
        }).name("Alert Logger");
    }

}