package com.example.detection.source;

import org.apache.flink.api.common.functions.MapFunction;

import com.example.detection.model.Event;
import com.example.detection.model.Transaction;

/**
 * TransactionMapper:
 * Đóng gói Event → Transaction
 */
public class TransactionMapper implements MapFunction<Event, Transaction> {
    @Override
    public Transaction map(Event event) {
        String key = String.valueOf(event.getId());
        long eventTs = event.getCreatedTime();
        long ingestTs = System.currentTimeMillis();

        return new Transaction(key, event, eventTs, ingestTs);
    }
}
