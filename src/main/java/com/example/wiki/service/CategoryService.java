package com.example.wiki.service;

import com.example.wiki.domain.Category;
import com.example.wiki.domain.CategoryExample;
import com.example.wiki.mapper.CategoryMapper;
import com.example.wiki.req.CategoryQueryReq;
import com.example.wiki.req.CategorySaveReq;
import com.example.wiki.resp.CategoryQueryResp;
import com.example.wiki.resp.PageResp;
import com.example.wiki.util.CopyUtil;
import com.example.wiki.util.SnowFlake;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.ObjectUtils;

import java.util.List;

@Service
public class CategoryService {

    @Autowired
    private CategoryMapper categoryMapper;

    @Autowired
    private SnowFlake snowFlake;

    private static final Logger LOG = LoggerFactory.getLogger(CategoryService.class);

    public PageResp<CategoryQueryResp> list(CategoryQueryReq req){
        CategoryExample categoryExample = new CategoryExample();
        CategoryExample.Criteria criteria = categoryExample.createCriteria();


        PageHelper.startPage(req.getPage(),req.getSize());
        List<Category> categorys = categoryMapper.selectByExample(categoryExample);

        PageInfo<Category> pageInfo = new PageInfo<>(categorys);
        LOG.info("总行数：{}",pageInfo.getTotal());
        LOG.info("总页数：{}",pageInfo.getPages());


//        List<CategoryResp> respList = new ArrayList<>();
//        for (Category category : categorys) {
////            CategoryResp categoryResp = new CategoryResp();
////            BeanUtils.copyProperties(category,categoryResp);
        //对象复制
//            CategoryResp categoryResp = CopyUtil.copy(category, CategoryResp.class);
//            respList.add(categoryResp);
//        }
        PageResp<CategoryQueryResp> pageResp = new PageResp<>();
        //列表复制
        List<CategoryQueryResp> respList = CopyUtil.copyList(categorys, CategoryQueryResp.class);
        pageResp.setTotal(pageInfo.getTotal());
        pageResp.setList(respList);
        return pageResp;
    }

    /**
     * 保存
     * @param req
     */
    public void save(CategorySaveReq req) {
        Category category = CopyUtil.copy(req, Category.class);
        if (ObjectUtils.isEmpty(category.getId())){
            //新增
            category.setId(snowFlake.nextId());
            categoryMapper.insert(category);
        }else{
            categoryMapper.updateByPrimaryKey(category);
        }
    }

    public void delete(Long id) {
        System.out.println(id);
        categoryMapper.deleteByPrimaryKey(id);
    }
}
