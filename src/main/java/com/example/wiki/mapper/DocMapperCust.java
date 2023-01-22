package com.example.wiki.mapper;

public interface DocMapperCust {
    public void increaseViewCount(Long id);

    void increaseVoteCount(Long id);

    void updateEbookInfo();
}
