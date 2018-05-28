package utils;

import core.web.blog.BlogEntry;

import java.util.Comparator;

public class BlogEntryTimestampComparator implements Comparator<BlogEntry> {


    @Override
    public int compare(BlogEntry one, BlogEntry two) {
        if (one.getTime() < two.getTime())
            return -1;
        else if (one.getTime() > two.getTime())
            return 1;
        else
            return 0;
    }

}
