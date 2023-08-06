package app.learning.web;

import app.learning.services.DistributedLockManager;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import org.jboss.resteasy.reactive.RestResponse;

import java.time.Duration;
import java.time.temporal.ChronoUnit;

@Path("/lock")
@ApplicationScoped
@Produces(MediaType.APPLICATION_JSON)
public class JobResource {

    @GET
    public RestResponse<?> showLocks(){
        return RestResponse.ok(distributedLockManager.fetch());
    }

    @Inject
    DistributedLockManager distributedLockManager;

    @Path("/{lockId}/{ownerId}")
    @POST
    public RestResponse<Boolean> lock(@PathParam("lockId") String lockId, @PathParam("ownerId") String ownerId){
        System.out.printf("acquire lockId [%s], ownerId [%s]%n", lockId, ownerId);
        boolean lockRes = distributedLockManager.createLock(lockId, Duration.of(30, ChronoUnit.SECONDS), ownerId);
        System.out.println("result = " + lockRes);
        return RestResponse.ok(lockRes);
    }

    @Path("/{lockId}/{ownerId}")
    @PUT
    public RestResponse<Boolean> renew(@PathParam("lockId") String lockId, @PathParam("ownerId") String ownerId){
        System.out.printf("acquire lockId [%s], ownerId [%s]%n", lockId, ownerId);
        boolean lockRes = distributedLockManager.createLock(lockId, Duration.of(30, ChronoUnit.SECONDS), ownerId);
        System.out.println("result = " + lockRes);
        return RestResponse.ok(lockRes);
    }

    @Path("/{lockId}/{ownerId}")
    @DELETE
    public RestResponse<Boolean> release(@PathParam("lockId") String lockId, @PathParam("ownerId") String ownerId){
        System.out.printf("acquire lockId [%s], ownerId [%s]%n", lockId, ownerId);
        boolean lockRes = distributedLockManager.releaseLock(lockId, ownerId);
        System.out.println("result = " + lockRes);
        return RestResponse.ok(lockRes);
    }

}
