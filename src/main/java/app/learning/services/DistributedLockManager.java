package app.learning.services;


import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoCursor;
import com.mongodb.client.model.IndexOptions;
import com.mongodb.client.result.DeleteResult;
import com.mongodb.client.result.InsertOneResult;
import com.mongodb.client.result.UpdateResult;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import org.bson.Document;

import java.time.Duration;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.TimeUnit;

@ApplicationScoped
public class DistributedLockManager {

    public static final String LOCK_EXPIRY_ATTR = "expireAt";
    final MongoCollection<Document> lockCollection;
    @Inject
    public DistributedLockManager(MongoClient mongoClient){
        lockCollection = mongoClient.getDatabase("test_db").getCollection("lock");
    }

    public void init(){
        var expireAtAttribute = new Document().append(LOCK_EXPIRY_ATTR, 1);
        var indexOptions = new IndexOptions().expireAfter(0L, TimeUnit.SECONDS);
        lockCollection.createIndex(expireAtAttribute, indexOptions);
    }

    public boolean createLock(final String lockId, Duration lockedDuration, final String lockedBy){
        try {
            long expiryEpochMs = new Date().getTime() + lockedDuration.getSeconds() * 1000;
            InsertOneResult insertOneResult = lockCollection.insertOne(
                    new Document("_id", lockId)
                            .append("locked_by", lockedBy)
                            .append(LOCK_EXPIRY_ATTR, new Date(expiryEpochMs)));
            return Objects.nonNull(insertOneResult.getInsertedId());
        } catch (Exception e){
            System.out.printf("failed to acquire lock with id %s%n", lockId);
            return false;
        }
    }

    public boolean renewLock(final String lockId, Duration renewDuration, final String lockedBy){
        try {
            long expiryEpochMs = new Date().getTime() + renewDuration.getSeconds() * 1000;
            UpdateResult updateResult = lockCollection.updateOne(
                    new Document("_id", lockId).append("locked_by", lockedBy),
                    new Document("$set", new Document(LOCK_EXPIRY_ATTR, new Date(expiryEpochMs)))
            );
            return updateResult.getModifiedCount()>0;
        } catch (Exception e){
            System.out.printf("failed to acquire lock with id %s%n", lockId);
            return false;
        }
    }

    public boolean releaseLock(final String lockId, final String lockedBy){
        try {
            DeleteResult deleteResult = lockCollection.deleteOne(new Document("_id", lockId));
            return deleteResult.getDeletedCount()>1;
        } catch (Exception e){
            System.out.printf("failed to release lock with id %s%n", lockId);
            return false;
        }
    }

    public List<Document> fetch() {
        List<Document> locks = new ArrayList<>();
        try(MongoCursor<Document> iterator = lockCollection.find().iterator()){
            iterator.forEachRemaining(locks::add);
        }
        return locks;
    }
}
