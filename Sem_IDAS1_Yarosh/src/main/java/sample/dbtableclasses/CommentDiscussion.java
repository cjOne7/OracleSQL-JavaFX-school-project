package sample.dbtableclasses;

public class CommentDiscussion {
    private Comment comment;
    private Discussion discussion;

    public CommentDiscussion(final Comment comment, final Discussion discussion) {
        this.comment = comment;
        this.discussion = discussion;
    }

    public Comment getComment() {
        return comment;
    }

    public Discussion getDiscussion() {
        return discussion;
    }

    @Override
    public String toString() {
        return "comment: " + comment +
                ", discussion: " + discussion;
    }
}
