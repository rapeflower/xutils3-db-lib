package org.xutils;

import android.app.Application;
import android.text.TextUtils;

import org.xutils.db.sqlite.WhereBuilder;
import org.xutils.db.table.TableEntity;
import org.xutils.ex.DbException;
import org.xutils.utils.KeyValue;

import java.io.File;
import java.util.List;

/***********
 * @Author rape flower
 * @Date 2018-02-26 16:52
 * @Describe 对外统一提供数据库的操作
 * <p>
 *     使用步骤：
 *     1、初始化：在应用程序启动时初始化，在Application的onCreate()中编写以下代码
 *     DbManager.initDbConfig(Application application, String dbName, int dbVersion, boolean isDebug);
 *     2、具体使用
 *     DbManager.xxx();//xxx：对应增删改查的方法
 * </p>
 */
public class DbManager {

    private static DbProxy db = null;
    private static String DB_NAME = "def_db";
    private static int DB_VERSION = 1;

    private DbManager() {

    }

    /**
     * 初始化数据库及数据库配置
     *
     * @param application 应用程序对象
     * @param dbName      数据库名称
     * @param dbVersion   数据库版本
     * @param isDebug     是否输出日志
     */
    public static void initDbConfig(Application application, String dbName, int dbVersion, boolean isDebug) {
        if (application == null) {
            return;
        }
        //初始化数据库
        x.Ext.init(application);
        //是否输出debug日志
        x.Ext.setDebug(isDebug);
        //数据库名称、版本号
        DB_NAME = dbName;
        DB_VERSION = dbVersion;

        //初始化DbProxy
        db = x.getDb(buildDaoConfig());
    }

    /**
     * DaoConfig
     *
     * @return
     */
    private static DbProxy.DaoConfig buildDaoConfig() {
        //数据库配置
        DbProxy.DaoConfig daoConfig = new DbProxy.DaoConfig()
                //设置数据库名
                .setDbName(DB_NAME)
                //设置数据库版本,每次启动应用时将会检查该版本号
                .setDbVersion(DB_VERSION)
                //设置是否开启事务,默认为false关闭事务
                .setAllowTransaction(true)
                .setTableCreateListener(new DbProxy.TableCreateListener() {
                    //设置数据库创建时的Listener
                    @Override
                    public void onTableCreated(DbProxy db, TableEntity<?> table) {

                    }
                })
                .setDbUpgradeListener(new DbProxy.DbUpgradeListener() {
                    //发现数据库版本低于这里设置的值将进行数据库升级并触发DbUpgradeListener
                    @Override
                    public void onUpgrade(DbProxy db, int oldVersion, int newVersion) {
                        if (newVersion > oldVersion) {
                            try {
                                db.dropDb();
                            } catch (DbException e) {
                                e.printStackTrace();
                            }
                        }
                    }
                });

        return daoConfig;
    }

    /**
     * 检测DbManager是不是初始化了
     */
    private static void checkDbManager() {
        if (db == null) {
            db = x.getDb(buildDaoConfig());
        }
    }

    /**
     * 获取DbProxy
     *
     * @return
     */
    public static DbProxy getDbProxy() {
        return db;
    }

    /**
     * 向数据库插入数据
     *
     * @param data 实体类或实体类的List
     */
    public static void insert(Object data) {
        try {
            checkDbManager();
            db.save(data);
        } catch (DbException e) {
            e.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /***
     * 插入唯一的对象，先删除，在插入,TODO 要看看这个id怎么变化
     * @param entity
     * @param entityClass
     * @param wb
     */
    public static void insert(Object entity, Class<?> entityClass, WhereBuilder wb) {
        if (wb == null) {
            insert(entity);
            return;
        }
        delete(entityClass, wb);
        insert(entity);
    }

    /**
     * 更新数据库中的数据
     *
     * @param entity            实体类
     * @param updateColumnNames 修改或更新的字段（对应数据库表列名）
     */
    public static void update(Object entity, String... updateColumnNames) {
        try {
            checkDbManager();
            db.update(entity, updateColumnNames);
        } catch (DbException e) {
            e.printStackTrace();
        } catch (Exception e) {
        }
    }

    /***
     * 根据条件更新
     * @param entityType
     * @param whereBuilder
     * @param nameValuePairs
     */
    public static void update(Class<?> entityType, WhereBuilder whereBuilder, KeyValue... nameValuePairs) {
        try {
            checkDbManager();
            db.update(entityType, whereBuilder, nameValuePairs);
        } catch (DbException e) {
            e.printStackTrace();
        } catch (Exception e) {
        }
    }

    /**
     * 查询第一条数据
     *
     * @param entityType 单个表的一条数据封装的类名
     * @return T 返回数据库中实体类型对应表的第一条数据
     */
    public static <T> T queryFirst(Class<T> entityType) {
        T t = null;
        try {
            t = db.findFirst(entityType);
        } catch (DbException e) {
            e.printStackTrace();
        } catch (Exception e) {
        }
        return t;
    }

    /**
     * 根据条件查询第一条数据
     *
     * @param entityType 单个表的一条数据封装的类名
     * @param wb         查询条件
     * @return T 返回数据库中符合条件的实体类型对应表的第一条数据
     */
    public static <T> T queryFirst(Class<T> entityType, WhereBuilder wb) {
        T t = null;
        try {
            t = db.selector(entityType).where(wb).findFirst();
        } catch (DbException e) {
            e.printStackTrace();
        } catch (Exception e) {
        }
        return t;
    }

    /**
     * 根据条件查询第一条数据
     *
     * @param entityType 单个表的一条数据封装的类名
     * @param wb         查询条件
     * @return T 返回数据库中符合条件的实体类型对应表的第一条数据
     */
    public static <T> T queryFirst(Class<T> entityType, WhereBuilder wb, String columnName) {
        T t = null;
        try {
            t = db.selector(entityType).where(wb).orderBy(columnName).findFirst();
        } catch (DbException e) {
            e.printStackTrace();
        } catch (Exception e) {
        }
        return t;
    }

    /**
     * 查询所有数据
     *
     * @param entityType 单个表的一条数据封装的类名
     * @return T
     */
    public static <T> List<T> queryAll(Class<T> entityType) {
        List<T> list = null;
        try {
            checkDbManager();
            list = db.findAll(entityType);
        } catch (DbException e) {
            e.printStackTrace();
        } catch (Exception e) {
        }

        return list;
    }

    /**
     * 查询所有数据
     *
     * @param entityType 单个表的一条数据封装的类名
     * @return T
     */
    public static <T> List<T> queryAll(Class<T> entityType, WhereBuilder wb) {
        List<T> list = null;
        try {
            checkDbManager();
            list = db.selector(entityType).where(wb).findAll();
        } catch (DbException e) {
            e.printStackTrace();
        } catch (Exception e) {
        }

        return list;
    }

    /**
     * 查询所有数据
     *
     * @param entityType        单个表的一条数据封装的类名
     * @param orderByColumnName 排序的列
     * @param limit             限时返回的个数
     * @return T
     */
    public static <T> List<T> queryAll(Class<T> entityType, WhereBuilder wb, String orderByColumnName, boolean desc, int limit) {
        List<T> list = null;
        try {
            checkDbManager();
            list = db.selector(entityType).where(wb).orderBy(orderByColumnName, desc).limit(limit).findAll();
        } catch (DbException e) {
            e.printStackTrace();
        } catch (Exception e) {
        }

        return list;
    }


    /**
     * 删除数据
     *
     * @param entityType 数据库表对应的实体类
     */
    public static void delete(Class<?> entityType) {
        try {
            checkDbManager();
            db.delete(entityType);
        } catch (DbException e) {
            e.printStackTrace();
        } catch (Exception e) {
        }
    }

    /**
     * 删除数据
     *
     * @param entityType 数据库表对应的实体类
     * @param wb         删除条件
     */
    public static void delete(Class<?> entityType, WhereBuilder wb) {
        try {
            checkDbManager();
            db.delete(entityType, wb);
        } catch (DbException e) {
            e.printStackTrace();
        } catch (Exception e) {
        }
    }
}
