package exercise4.cola;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.File;
import java.io.IOException;

import xxl.core.collections.containers.io.BlockFileContainer;
import xxl.core.cursors.sources.io.FileInputCursor;
import xxl.core.io.converters.Converter;
import xxl.core.io.converters.DoubleConverter;
import xxl.core.io.converters.LongConverter;
import xxl.core.util.Pair;

public class Main {
    public static void main(String[] args) {

        BlockFileContainer raw = new BlockFileContainer("COLA", BasicCOLA.DISK_BLOCK_SIZE);

        BasicCOLA<Long, Double> cola = new BasicCOLA<>(
                LongConverter.SIZE + DoubleConverter.SIZE,
                LongConverter.DEFAULT_INSTANCE,
                DoubleConverter.DEFAULT_INSTANCE,
                raw);

        FileInputCursor<Pair<Long, Double>> input = new FileInputCursor<>(new Converter<>() {
            @Override
            public Pair<Long, Double> read(DataInput dataInput,
                                           Pair<Long, Double> object) throws IOException {
                return new Pair<>(dataInput.readLong(), dataInput.readDouble());
            }

            @Override
            public void write(DataOutput dataOutput, Pair<Long, Double> object)
                    throws IOException {
                dataOutput.writeLong(object.getFirst());
                dataOutput.writeDouble(object.getSecond());
            }
        }, new File("timeseries.bin"));

        input.open();
        long firstKey = 0, lastKey = 0;
//        for (int i = 0; input.hasNext(); i++) {
//            Pair<Long, Double> p = input.next();
//            cola.insertElement(p.getFirst(), p.getSecond());
//            if (i < 10) {
//                System.out.println("Inserting: " + p);
//                cola.printLevels();
//            }
//        }

        for (int i = 0; input.hasNext(); i++) {
            Pair<Long, Double> p = input.next();
            // sets firstKey only for the first element
            if (i == 0) firstKey = p.getFirst();
            // updating lastKey for every element
            lastKey = p.getFirst();
            cola.insertElement(p.getFirst(), p.getSecond());
            if (i < 10) {
                System.out.println("Inserting: " + p);
                cola.printLevels();
            }
        }
        input.close();

        // Search for the first and last inserted elements
//        long key = 1325458800000L;
//        System.out.println("Searching key: " + key + ", result: " + cola.searchElement(key));
//        key = 1262127600000L;
//        System.out.println("Searching key: " + key + ", result: " + cola.searchElement(key));

        // TODO: benchmark the search for the first and the last inserted keys

        int iterations = 1000;
        long startTime, endTime;
        double totalFirstSearch = 0, totalLastSearch = 0;

        for (int i = 0; i < iterations; i++) {
            startTime = System.nanoTime();
            cola.searchElement(firstKey);
            endTime = System.nanoTime();
            totalFirstSearch += (endTime - startTime);

            startTime = System.nanoTime();
            cola.searchElement(lastKey);
            endTime = System.nanoTime();
            totalLastSearch += (endTime - startTime);
        }

        System.out.println("Average search time for first key: " + (totalFirstSearch / iterations) + " ns");
        System.out.println("Average search time for last key: " + (totalLastSearch / iterations) + " ns");

        cola.close();
    }
}
