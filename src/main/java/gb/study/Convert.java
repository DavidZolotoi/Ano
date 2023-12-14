package gb.study;

import java.sql.Timestamp;

public interface Convert {
    //todo ДОДЕЛАТЬ ВСЕ РЕАЛИЗАЦИИ, сейчас все черновое
    default Integer objectToInteger(Object obj) {
        if (obj instanceof Integer) {
            return (Integer)obj;
        } else {
            String objStr = obj.toString();
            return Integer.parseInt(objStr);
        }
    }

    default String objectToString(Object obj) {
        if (obj instanceof String) {
            return obj.toString();
        } else {
            String objStr = obj.toString();
            return objStr;
        }
    }

    default Timestamp objectToTimestamp(Object obj) {
        if (obj instanceof Timestamp) {
            return (Timestamp)obj;
        } else {
            String objStr = obj.toString();
            return Timestamp.valueOf(objStr);
        }
    }
}
