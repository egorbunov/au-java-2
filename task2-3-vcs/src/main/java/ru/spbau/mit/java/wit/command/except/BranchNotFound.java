package ru.spbau.mit.java.wit.command.except;


public class BranchNotFound extends RuntimeException {
    private String branchName;

    public BranchNotFound(String name) {

        this.branchName = name;
    }

    public String getBranchName() {
        return branchName;
    }
}
