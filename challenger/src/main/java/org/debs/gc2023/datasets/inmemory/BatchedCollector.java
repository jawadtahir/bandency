package org.debs.gc2023.datasets.inmemory;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.Random;

import org.debs.gc2023.datasets.IDataStore;
import org.rocksdb.RocksDBException;
import org.tinylog.Logger;

import org.debs.gc2023.bandency.Batch;
import org.debs.gc2023.bandency.DriveState;

public class BatchedCollector {
    private int maxBatchSize;
    private int currentBatchSize;
    private Batch.Builder bb;
    private int batchCount;
    private int maxBatches;
    private Random random;
    private IDataStore store;
    private long nextDate;
    private final int[] vaultValues = {1000, 1001, 1002, 1003, 1004, 1005, 1006, 1007, 1008, 1009,
            1010, 1011, 1012, 1013, 1014, 1015, 1016, 1017, 1018, 1019, 1020, 1021, 1022, 1023,
            1024, 1025, 1026, 1027, 1028, 1029, 1030, 1031, 1032, 1033, 1034, 1035, 1036, 1037,
            1038, 1039, 1040, 1041, 1042, 1043, 1044, 1045, 1046, 1047, 1048, 1049, 1050, 1051,
            1052, 1053, 1054, 1055, 1056, 1057, 1058, 1059, 1060, 1061, 1062, 1063, 1064, 1065,
            1066, 1067, 1068, 1069, 1070, 1071, 1072, 1073, 1074, 1075, 1076, 1077, 1078, 1079,
            1080, 1081, 1082, 1084, 1085, 1086, 1087, 1088, 1089, 1090, 1091, 1092, 1093, 1094,
            1095, 1096, 1097, 1098, 1099, 1100, 1101, 1102, 1103, 1104, 1105, 1106, 1107, 1108,
            1109, 1110, 1111, 1112, 1113, 1114, 1115, 1116, 1117, 1118, 1119, 1120, 1121, 1122,
            1123, 1124, 1125, 1126, 1127, 1128, 1129, 1130, 1131, 1132, 1133, 1134, 1135, 1136,
            1137, 1138, 1139, 1140, 1141, 1142, 1143, 1144, 1145, 1146, 1147, 1148, 1149, 1150,
            1151, 1152, 1153, 1154, 1155, 1156, 1157, 1158, 1159, 1160, 1161, 1162, 1163, 1164,
            1165, 1166, 1167, 1168, 1169, 1170, 1176, 1400, 1401, 1402, 1403, 1406, 1407, 1408,
            1409, 2000, 2001, 2002, 2003, 2004, 2005, 2006, 2007, 2008, 2009, 2010, 2011, 2012,
            2013, 2014, 2015, 2016, 2017, 2018, 2019, 2020, 2021, 3000, 3001, 3002, 300};

    public BatchedCollector(IDataStore store, int maxBatchSize, int maxBatches) {
        this.store = store;
        this.maxBatches = maxBatches;
        this.maxBatchSize = maxBatchSize;
        this.currentBatchSize = 0;
        this.batchCount = 0;

        this.random = new Random(42);
        this.nextDate = -1;
    }

    private void ensureBatch() {
        if (bb == null) {
            bb = Batch.newBuilder();
            bb.setSeqId(this.batchCount);
            bb.setDayEnd(false); // default false
        }
    }

    // Returns true if we can continue collecting, false if we should stop
    public boolean collectState(DriveState.Builder state, HashSet<String> models)
            throws RocksDBException {
        if (currentBatchSize >= maxBatchSize) {
            bb.setLast(false);

            if (this.batchCount % 100 == 0) {
                Logger.info("Collected batches: " + this.batchCount);
            }

            // Populate vault_ids with 5 distinct random values
            random.ints(vaultValues.length, 0, vaultValues.length).distinct().limit(5)
                    .forEach(i -> bb.addVaultIds(vaultValues[i]));

            // Randomly select 5 distinct cluster ids between 0 and 50
            random.ints(50, 0, 50).distinct().limit(5).forEach(i -> bb.addClusterIds(i));

            store.AddBatch(this.batchCount, bb.build());
            store.SetBatchCount(this.batchCount);
            bb = null;
            ++this.batchCount;
            currentBatchSize = 0;

            if (this.maxBatches > 0 && this.batchCount >= maxBatches) {
                return false;
            }
        }
        // ensure the batch is initialized
        ensureBatch();

        // ensure the next day treshold is set to mark batches with end of day
        if (nextDate == -1) {
            nextDate = state.getDate().getSeconds() + 24 * 60 * 60;
        }

        // usual operation
        bb.addStates(state);
        ++currentBatchSize;

        // set dayend to true if we are at the end of the day
        if (state.getDate().getSeconds() >= nextDate) {
            bb.setDayEnd(true);
            nextDate = state.getDate().getSeconds() + 24 * 60 * 60;
        }

        return true;
    }

    public void close() throws RocksDBException {
        if (bb != null) {
            bb.setLast(true);
            this.store.AddBatch(this.batchCount, bb.build());
            bb = null;
            currentBatchSize = 0;
        }
    }

    public int batchCount() throws RocksDBException, InterruptedException {
        return this.store.BatchCount();
    }
}
