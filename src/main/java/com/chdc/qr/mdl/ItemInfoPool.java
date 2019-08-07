package com.chdc.qr.mdl;

import com.chdc.qr.QRResources;
import com.chdc.qr.lib.CustomDispatcher;
import com.chdc.qr.lib.db.QueryManager;
import lombok.extern.slf4j.Slf4j;

import javax.swing.*;
import java.sql.SQLException;
import java.util.Hashtable;
import java.util.List;

@Slf4j
public class ItemInfoPool implements QRResources {
    private static final class Creator {
        private static final ItemInfoPool inst = new ItemInfoPool();
    }

    private final static Hashtable<String, ItemInfo> hashItem = new Hashtable<>();

    private ItemInfoPool() {
        loadFromDb();
    }

    public static ItemInfoPool getInstance() {
        return Creator.inst;
    }

    private void loadFromDb() {
        QueryManager qm = new QueryManager();
        try {
            List<List> lists = qm.executeQuery("SELECT CODE, TYPE, CATEGORY, NAME FROM ITEM_INFO");
            lists.remove(0);
            int count = 0;
            for (List record : lists) {
                put(new ItemInfo(
                        qm.getInt(record.get(1)),
                        qm.getInt(record.get(0)),
                        qm.getString(record.get(2)),
                        qm.getString(record.get(3)))
                );
                count++;
            }

            log.info("[ItemInfo] loaded : " + count);
        } catch (SQLException e) {
            CustomDispatcher.invokeAndWait(() -> {
                JOptionPane.showMessageDialog(null, "수입항목 코드 조회 오류-" + e.getMessage());
            });
        }
    }

    public void put(ItemInfo itemInfo) {
        hashItem.put(itemInfo.getType() + "-" + itemInfo.getCode(), itemInfo);
    }

    public ItemInfo get(int type, int code) {
        return hashItem.get(type + "-" + code);
    }
}
