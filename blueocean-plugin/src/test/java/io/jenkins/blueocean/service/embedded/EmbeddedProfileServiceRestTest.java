package io.jenkins.blueocean.service.embedded;

import com.google.common.collect.ImmutableList;
import com.jayway.restassured.RestAssured;
import com.jayway.restassured.response.Response;
import hudson.model.User;
import hudson.tasks.Mailer;
import io.jenkins.blueocean.service.embedded.rest.OrganizationContainerImpl;
import org.hamcrest.Matchers;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.jvnet.hudson.test.JenkinsRule;

import javax.inject.Inject;
import java.util.List;

/**
 * @author Vivek Pandey
 */
public class EmbeddedProfileServiceRestTest {
    @Rule
    public JenkinsRule j = new JenkinsRule();

    @Inject
    public OrganizationContainerImpl orgContainer;

    @Before
    public void before() {
        RestAssured.baseURI = j.jenkins.getRootUrl()+"bo/rest";
        RestAssured.enableLoggingOfRequestAndResponseIfValidationFails();
        j.jenkins.getInjector().injectMembers(this);
    }

    @Test
    public void getUserTest() throws Exception {
        User system = j.jenkins.getUser("SYSTEM");

        RestAssured.given().log().all().get("/users/{id}/", system.getId())
            .then().log().all()
            .statusCode(200)
            .body("id", Matchers.equalTo(system.getId()))
            .body("fullName", Matchers.equalTo(system.getFullName()));
    }

    @Test
    public void getUserDetailsTest() throws Exception {
        hudson.model.User user = j.jenkins.getUser("alice");
        user.setFullName("Alice Cooper");
        user.addProperty(new Mailer.UserProperty("alice@jenkins-ci.org"));

        RestAssured.given().log().all().get("/users/{id}/",user.getId())
            .then().log().all()
            .statusCode(200)
            .body("id", Matchers.equalTo(user.getId()))
            .body("fullName", Matchers.equalTo(user.getFullName()))
            .body("email", Matchers.equalTo("alice@jenkins-ci.org"));
    }

    @Test
    public void getOrganizationTest(){
        RestAssured.given().log().all().get("/organizations/jenkins")
            .then().log().all()
            .statusCode(200)
            .body("name", Matchers.equalTo("jenkins"));
    }

    @Test
    public void FindUsersTest() throws Exception {
        List<String> names = ImmutableList.of("alice", "bob");
        j.jenkins.getUser(names.get(0));
        j.jenkins.getUser(names.get(1));

        Response response = RestAssured.given().log().all().get("/search?q=type:user;organization:jenkins");

        response.then().log().all().statusCode(200);

        Assert.assertTrue(names.contains((String)response.path("users[0].id")));
        Assert.assertTrue(names.contains((String)response.path("users[0].name")));
        Assert.assertTrue(names.contains((String)response.path("users[1].id")));
        Assert.assertTrue(names.contains((String)response.path("users[1].name")));
    }



}
