package com.hpe.alm.octane;

import cucumber.api.PendingException;
import cucumber.api.java.en.And;
import cucumber.api.java.en.Given;
import cucumber.api.java.en.Then;
import cucumber.api.java.en.When;

/**
 * Created by intract on 27/06/2016.
 */
public class MyStepdefs {
    @Given("^hello \"([^\"]*)\"$")
    public void hello(String arg0) throws Throwable {
        // Write code here that turns the phrase above into concrete actions
        //throw new PendingException();
    }

    @When("^what \"([^\"]*)\"$")
    public void what(String arg0) throws Throwable {
        // Write code here that turns the phrase above into concrete actions
        //throw new PendingException();
    }

    @Then("^wow$")
    public void wow() throws Throwable {
        // Write code here that turns the phrase above into concrete actions
        //throw new PendingException();
    }

    @Given("^back(\\d+)$")
    public void back(int arg0) throws Throwable {
        // Write code here that turns the phrase above into concrete actions
        //throw new PendingException();
    }

    @Given("^test(\\d+)$")
    public void test(int arg0) throws Throwable {
        // Write code here that turns the phrase above into concrete actions
        //throw new PendingException();
    }

    @Given("^back$")
    public void back() throws Throwable {
        // Write code here that turns the phrase above into concrete actions
        //throw new PendingException();
    }

    @Given("^test$")
    public void test() throws Throwable {
        // Write code here that turns the phrase above into concrete actions
        //throw new PendingException();
    }


    @Given("^I am logged in$")
    public void i_am_logged_in() throws Throwable {
    }

    @When("^pending step$")
    public void pending_step() throws Throwable {
        // Write code here that turns the phrase above into concrete actions
        throw new PendingException();
    }

    @Then("^another pending step$")public void another_pending_step() throws Throwable {
        // Write code here that turns the phrase above into concrete actions
        throw new PendingException();
    }

    @And("^I'm failed$")
    public void iMFailed() throws Throwable {
        // Write code here that turns the phrase above into concrete actions
        throw new Exception("exception happened");
    }

    @When("^passed step$")
    public void passedStep() throws Throwable {

    }

    @Then("^another passed step$")
    public void anotherPassedStep() throws Throwable {

    }

    @When("^failed step$")
    public void failedStep() throws Throwable {
        iMFailed();
    }

    @Then("^another failed step$")
    public void anotherFailedStep() throws Throwable {
        failedStep();
    }
}
