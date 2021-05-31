package com.bestfriend.schema;

import com.codepan.database.Convention;
import com.codepan.database.Field;
import com.codepan.database.IndexValue;
import com.codepan.database.SQLiteQuery;
import com.codepan.database.SQLiteQuery.Constraint;
import com.codepan.database.SQLiteQuery.DataType;
import com.codepan.database.TableIndices;

public class Tables {

    public enum TB {
        REMITTANCE,
        CUSTOMER,
        RECEIVE,
        TRANSFER
    }

    public static SQLiteQuery fields(TB tb) {
        SQLiteQuery query = new SQLiteQuery();
        switch(tb) {
            case CUSTOMER:
                query.add(new Field("ID", Constraint.PRIMARY_KEY));
                query.add(new Field("name", DataType.TEXT));
                query.add(new Field("photo", DataType.TEXT));
                query.add(new Field("address", DataType.TEXT));
                query.add(new Field("mobileNo", DataType.TEXT));
                query.add(new Field("isActive", 1));
                break;
            case REMITTANCE:
                query.add(new Field("ID", Constraint.PRIMARY_KEY));
                query.add(new Field("dDate", DataType.TEXT));
                query.add(new Field("dTime", DataType.TEXT));
                query.add(new Field("smDate", DataType.TEXT));
                query.add(new Field("smTime", DataType.TEXT));
                query.add(new Field("charge", DataType.TEXT));
                query.add(new Field("type", DataType.INTEGER));
                query.add(new Field("amount", DataType.TEXT));
                query.add(new Field("referenceNo", DataType.TEXT));
                query.add(new Field("balance", DataType.TEXT));
                query.add(new Field("mobileNo", DataType.TEXT));
                query.add(new Field("isClaimed", 0));
                break;
            case RECEIVE:
                query.add(new Field("ID", Constraint.PRIMARY_KEY));
                query.add(new Field("dDate", DataType.TEXT));
                query.add(new Field("dTime", DataType.TEXT));
                query.add(new Field("customerID", DataType.INTEGER));
                query.add(new Field("remittanceID", DataType.INTEGER));
                query.add(new Field("isCancelled", 0));
                break;
            case TRANSFER:
                query.add(new Field("ID", Constraint.PRIMARY_KEY));
                query.add(new Field("dDate", DataType.TEXT));
                query.add(new Field("dTime", DataType.TEXT));
                query.add(new Field("customerID", DataType.INTEGER));
                query.add(new Field("remittanceID", DataType.INTEGER));
                query.add(new Field("receiver", DataType.TEXT));
                query.add(new Field("isCancelled", 0));
                break;
        }
        return query;
    }

    public static TableIndices indexes(TB tb) {
        TableIndices indices = null;
        String name = getName(tb);
        String suffix = getIndexSuffix(tb);
        switch(tb) {
            case REMITTANCE:
                indices = new TableIndices(
                    name,
                    new IndexValue(
                        "date_" + suffix,
                        new Field("smDate")
                    ),
                    new IndexValue(
                        "ref_" + suffix,
                        new Field("referenceNo")
                    ),
                    new IndexValue(
                        "date_ref_" + suffix,
                        new Field("smDate"),
                        new Field("referenceNo")
                    )
                );
                break;
            case TRANSFER:
            case RECEIVE:
                indices = new TableIndices(
                    name,
                    new IndexValue(
                        "rem_" + suffix,
                        new Field("remittanceID")
                    )
                );
                break;
        }
        return indices;
    }

    public static String getIndexSuffix(TB tb) {
        if(tb != null) {
            String suffix = Convention.INDEX_SUFFIX;
            return tb.toString()
                .toLowerCase()
                .concat(suffix);
        }
        return null;
    }

    public static String getName(TB tb) {
        if(tb != null) {
            String suffix = Convention.TABLE_SUFFIX;
            return tb.toString()
                .toLowerCase()
                .concat(suffix);
        }
        return null;
    }
}
