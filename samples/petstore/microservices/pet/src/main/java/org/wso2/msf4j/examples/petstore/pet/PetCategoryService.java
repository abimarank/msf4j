/*
 * Copyright (c) 2016, WSO2 Inc. (http://wso2.com) All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.wso2.msf4j.examples.petstore.pet;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.wso2.carbon.metrics.annotation.Timed;
import org.wso2.msf4j.examples.petstore.util.JedisUtil;
import org.wso2.msf4j.examples.petstore.util.model.Category;
import org.wso2.msf4j.analytics.httpmonitoring.HTTPMonitored;

import java.util.HashSet;
import java.util.Set;
import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Response;

/**
 * Pet category microservice.
 */
@HTTPMonitored
@Path("/category")
public class PetCategoryService {
    private static final Logger log = LoggerFactory.getLogger(PetCategoryService.class);

    @POST
    @Consumes("application/json")
    @Timed
    public Response addCategory(Category category) {
        String name = category.getName();
        JedisUtil.sadd(org.wso2.msf4j.examples.petstore.pet.PetConstants.CATEGORIES_KEY, name);
        log.info("Added category");
        return Response.status(Response.Status.OK).entity("Category with name " + name + " successfully added").build();
    }

    @DELETE
    @Path("/{name}")
    @Timed
    public Response deleteCategory(@PathParam("name") String name) {
        if (!JedisUtil.smembers(PetConstants.CATEGORIES_KEY).contains(name)) {
            return Response.status(Response.Status.NOT_FOUND).build();
        }
        String categoryKey = PetConstants.CATEGORY_KEY_PREFIX + name;
        JedisUtil.srem(PetConstants.CATEGORIES_KEY, name);
        JedisUtil.del(categoryKey);
        log.info("Deleted category: " + name);
        return Response.status(Response.Status.OK).entity("OK").build();
    }

    @GET
    @Produces("application/json")
    @Path("/{name}")
    @Timed
    public Response getCategory(@PathParam("name") String name) {
        if (!JedisUtil.smembers(PetConstants.CATEGORIES_KEY).contains(name)) {
            return Response.status(Response.Status.NOT_FOUND).build();
        }
        log.info("Got category");
        return Response.status(Response.Status.OK).entity(new Category(name)).build();
    }

    @GET
    @Path("/all")
    @Produces("application/json")
    @Timed
    public Set<Category> getAllCategories() {
        Set<String> smembers = JedisUtil.smembers(PetConstants.CATEGORIES_KEY);
        Set<Category> categories = new HashSet<>(smembers.size());
        for (String smember : smembers) {
            categories.add(new Category(smember));
        }
        return categories;
    }

}
