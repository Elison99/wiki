package com.example.wiki.service;

import com.example.wiki.domain.Content;
import com.example.wiki.domain.Doc;
import com.example.wiki.domain.DocExample;
import com.example.wiki.exception.BusinessException;
import com.example.wiki.exception.BusinessExceptionCode;
import com.example.wiki.mapper.ContentMapper;
import com.example.wiki.mapper.DocMapper;
import com.example.wiki.mapper.DocMapperCust;
import com.example.wiki.req.DocQueryReq;
import com.example.wiki.req.DocSaveReq;
import com.example.wiki.resp.DocQueryResp;
import com.example.wiki.resp.PageResp;
import com.example.wiki.util.CopyUtil;
import com.example.wiki.util.RedisUtil;
import com.example.wiki.util.RequestContext;
import com.example.wiki.util.SnowFlake;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.ObjectUtils;

import java.util.List;

@Service
public class DocService {

    @Autowired
    private DocMapper docMapper;

    @Autowired
    private SnowFlake snowFlake;

    @Autowired
    private ContentMapper contentMapper;

    @Autowired
    private DocMapperCust docMapperCust;

    @Autowired
    private RedisUtil redisUtil;


    @Autowired
    private WsService wsService;

    private static final Logger LOG = LoggerFactory.getLogger(DocService.class);

    public List<DocQueryResp> all(){
        DocExample docExample = new DocExample();
        docExample.setOrderByClause("sort asc");
        List<Doc> docList = docMapper.selectByExample(docExample);
        //列表复制
        List<DocQueryResp> list = CopyUtil.copyList(docList, DocQueryResp.class);

        return list;
    }
    public List<DocQueryResp> all(Long ebookId){
        DocExample docExample = new DocExample();
        docExample.setOrderByClause("sort asc");
        docExample.createCriteria().andEbookIdEqualTo(ebookId);
        List<Doc> docList = docMapper.selectByExample(docExample);
        //列表复制
        List<DocQueryResp> list = CopyUtil.copyList(docList, DocQueryResp.class);

        return list;
    }

    public PageResp<DocQueryResp> list(DocQueryReq req){
        DocExample docExample = new DocExample();
        docExample.setOrderByClause("sort asc");
        DocExample.Criteria criteria = docExample.createCriteria();


        PageHelper.startPage(req.getPage(),req.getSize());
        List<Doc> docs = docMapper.selectByExample(docExample);

        PageInfo<Doc> pageInfo = new PageInfo<>(docs);
        LOG.info("总行数：{}",pageInfo.getTotal());
        LOG.info("总页数：{}",pageInfo.getPages());


//        List<DocResp> respList = new ArrayList<>();
//        for (Doc doc : docs) {
////            DocResp docResp = new DocResp();
////            BeanUtils.copyProperties(doc,docResp);
        //对象复制
//            DocResp docResp = CopyUtil.copy(doc, DocResp.class);
//            respList.add(docResp);
//        }
        PageResp<DocQueryResp> pageResp = new PageResp<>();
        //列表复制
        List<DocQueryResp> respList = CopyUtil.copyList(docs, DocQueryResp.class);
        pageResp.setTotal(pageInfo.getTotal());
        pageResp.setList(respList);
        return pageResp;
    }

    /**
     * 保存
     * @param req
     */
    @Transactional
    public void save(DocSaveReq req) {
        Doc doc = CopyUtil.copy(req, Doc.class);
        Content content = CopyUtil.copy(req, Content.class);
        if (ObjectUtils.isEmpty(doc.getId())){
            //新增
            doc.setId(snowFlake.nextId());
            doc.setViewCount(0);
            doc.setVoteCount(0);
            docMapper.insert(doc);

            content.setId(doc.getId());
            contentMapper.insert(content);
        }else{
            docMapper.updateByPrimaryKey(doc);
            int count = contentMapper.updateByPrimaryKeyWithBLOBs(content);
            if (count == 0){
                contentMapper.insert(content);
            }
        }
    }

    public void delete(Long id) {
        System.out.println(id);
        docMapper.deleteByPrimaryKey(id);
    }
    public void delete(List<String> ids) {
        DocExample docExample = new DocExample();
        DocExample.Criteria criteria = docExample.createCriteria();
        criteria.andIdIn(ids);
        docMapper.deleteByExample(docExample);
    }

    public String findContent(Long id) {
        Content content = contentMapper.selectByPrimaryKey(id);
        //文档阅读数加一
        docMapperCust.increaseViewCount(id);
        if (ObjectUtils.isEmpty(content)){
            return "";
        }else{
            return content.getContent();
        }
    }

    public void vote(Long id) {
        // docMapperCust.increaseVoteCount(id);
        // 远程IP+doc.id作为key，24小时内不能重复
        String ip = RequestContext.getRemoteAddr();
        if (redisUtil.validateRepeat("DOC_VOTE_" + id + "_" + ip, 5000)) {
            docMapperCust.increaseVoteCount(id);
        } else {
            throw new BusinessException(BusinessExceptionCode.VOTE_REPEAT);
        }

        Doc docDb = docMapper.selectByPrimaryKey(id);
        String logId = MDC.get("LOG_ID");
        // 推送消息
        wsService.sendInfo("【" + docDb.getName() + "】被点赞！",logId);
        // rocketMQTemplate.convertAndSend("VOTE_TOPIC", "【" + docDb.getName() + "】被点赞！");
    }

//    @Async
//    public void sendInfo(Long id){
//        Doc docDb = docMapper.selectByPrimaryKey(id);
//        String logId = MDC.get("LOG_ID");
//        webSocketServer.sendInfo("【" + docDb.getName() + "】被点赞！");
//    }

    public void updateEbookInfo() {
        docMapperCust.updateEbookInfo();
    }
}
