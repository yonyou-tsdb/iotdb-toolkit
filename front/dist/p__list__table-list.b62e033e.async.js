(self.webpackChunkant_design_pro=self.webpackChunkant_design_pro||[]).push([[4747],{68057:function(ye,P,r){"use strict";r.r(P),r.d(P,{default:function(){return me}});var ge=r(57338),N=r(26272),Ce=r(57663),R=r(71577),Ze=r(47673),z=r(4107),K=r(93224),j=r(2824),y=r(11849),je=r(34792),p=r(48086),h=r(3182),H=r(94043),l=r.n(H),J=r(49101),g=r(67294),Q=r(75362),X=r(85224),k=r(16894),q=r(37476),V=r(5966),I=r(90672),_=r(12826),Se=r(71194),ee=r(63254),x=r(65554),w=r(64317),re=r(86615),ue=r(22452),u=r(85893),te=function(a){return(0,u.jsxs)(x.L,{stepsProps:{size:"small"},stepsFormRender:function(t,c){return(0,u.jsx)(ee.Z,{width:640,bodyStyle:{padding:"32px 40px 48px"},destroyOnClose:!0,title:"\u89C4\u5219\u914D\u7F6E",visible:a.updateModalVisible,footer:c,onCancel:function(){a.onCancel()},children:t})},onFinish:a.onSubmit,children:[(0,u.jsxs)(x.L.StepForm,{initialValues:{name:a.values.name,desc:a.values.desc},title:"\u57FA\u672C\u4FE1\u606F",children:[(0,u.jsx)(V.Z,{name:"name",label:"\u89C4\u5219\u540D\u79F0",width:"md",rules:[{required:!0,message:"\u8BF7\u8F93\u5165\u89C4\u5219\u540D\u79F0\uFF01"}]}),(0,u.jsx)(I.Z,{name:"desc",width:"md",label:"\u89C4\u5219\u63CF\u8FF0",placeholder:"\u8BF7\u8F93\u5165\u81F3\u5C11\u4E94\u4E2A\u5B57\u7B26",rules:[{required:!0,message:"\u8BF7\u8F93\u5165\u81F3\u5C11\u4E94\u4E2A\u5B57\u7B26\u7684\u89C4\u5219\u63CF\u8FF0\uFF01",min:5}]})]}),(0,u.jsxs)(x.L.StepForm,{initialValues:{target:"0",template:"0"},title:"\u914D\u7F6E\u89C4\u5219\u5C5E\u6027",children:[(0,u.jsx)(w.Z,{name:"target",width:"md",label:"\u76D1\u63A7\u5BF9\u8C61",valueEnum:{0:"\u8868\u4E00",1:"\u8868\u4E8C"}}),(0,u.jsx)(w.Z,{name:"template",width:"md",label:"\u89C4\u5219\u6A21\u677F",valueEnum:{0:"\u89C4\u5219\u6A21\u677F\u4E00",1:"\u89C4\u5219\u6A21\u677F\u4E8C"}}),(0,u.jsx)(re.Z.Group,{name:"type",label:"\u89C4\u5219\u7C7B\u578B",options:[{value:"0",label:"\u5F3A"},{value:"1",label:"\u5F31"}]})]}),(0,u.jsxs)(x.L.StepForm,{initialValues:{type:"1",frequency:"month"},title:"\u8BBE\u5B9A\u8C03\u5EA6\u5468\u671F",children:[(0,u.jsx)(ue.Z,{name:"time",width:"md",label:"\u5F00\u59CB\u65F6\u95F4",rules:[{required:!0,message:"\u8BF7\u9009\u62E9\u5F00\u59CB\u65F6\u95F4\uFF01"}]}),(0,u.jsx)(w.Z,{name:"frequency",label:"\u76D1\u63A7\u5BF9\u8C61",width:"md",valueEnum:{month:"\u6708",week:"\u5468"}})]})]})},ae=te,A=r(21010);function xe(d,a){return B.apply(this,arguments)}function B(){return B=_asyncToGenerator(_regeneratorRuntime.mark(function d(a,i){return _regeneratorRuntime.wrap(function(c){for(;;)switch(c.prev=c.next){case 0:return c.abrupt("return",request("/api/rule",_objectSpread({method:"GET",params:_objectSpread({},a)},i||{})));case 1:case"end":return c.stop()}},d)})),B.apply(this,arguments)}function ne(d){return T.apply(this,arguments)}function T(){return T=(0,h.Z)(l().mark(function d(a){return l().wrap(function(t){for(;;)switch(t.prev=t.next){case 0:return t.abrupt("return",(0,A.WY)("/api/rule",(0,y.Z)({method:"PUT"},a||{})));case 1:case"end":return t.stop()}},d)})),T.apply(this,arguments)}function se(d){return b.apply(this,arguments)}function b(){return b=(0,h.Z)(l().mark(function d(a){return l().wrap(function(t){for(;;)switch(t.prev=t.next){case 0:return t.abrupt("return",(0,A.WY)("/api/rule",(0,y.Z)({method:"POST"},a||{})));case 1:case"end":return t.stop()}},d)})),b.apply(this,arguments)}function le(d){return $.apply(this,arguments)}function $(){return $=(0,h.Z)(l().mark(function d(a){return l().wrap(function(t){for(;;)switch(t.prev=t.next){case 0:return t.abrupt("return",(0,A.WY)("/api/rule",(0,y.Z)({method:"DELETE"},a||{})));case 1:case"end":return t.stop()}},d)})),$.apply(this,arguments)}var ie=function(){var d=(0,h.Z)(l().mark(function a(i){var t;return l().wrap(function(e){for(;;)switch(e.prev=e.next){case 0:return t=p.default.loading("\u6B63\u5728\u6DFB\u52A0"),e.prev=1,e.next=4,se((0,y.Z)({},i));case 4:return t(),p.default.success("\u6DFB\u52A0\u6210\u529F"),e.abrupt("return",!0);case 9:return e.prev=9,e.t0=e.catch(1),t(),p.default.error("\u6DFB\u52A0\u5931\u8D25\u8BF7\u91CD\u8BD5\uFF01"),e.abrupt("return",!1);case 14:case"end":return e.stop()}},a,null,[[1,9]])}));return function(i){return d.apply(this,arguments)}}(),de=function(){var d=(0,h.Z)(l().mark(function a(i){var t;return l().wrap(function(e){for(;;)switch(e.prev=e.next){case 0:return t=p.default.loading("\u6B63\u5728\u914D\u7F6E"),e.prev=1,e.next=4,ne({name:i.name,desc:i.desc,key:i.key});case 4:return t(),p.default.success("\u914D\u7F6E\u6210\u529F"),e.abrupt("return",!0);case 9:return e.prev=9,e.t0=e.catch(1),t(),p.default.error("\u914D\u7F6E\u5931\u8D25\u8BF7\u91CD\u8BD5\uFF01"),e.abrupt("return",!1);case 14:case"end":return e.stop()}},a,null,[[1,9]])}));return function(i){return d.apply(this,arguments)}}(),oe=function(){var d=(0,h.Z)(l().mark(function a(i){var t;return l().wrap(function(e){for(;;)switch(e.prev=e.next){case 0:if(t=p.default.loading("\u6B63\u5728\u5220\u9664"),i){e.next=3;break}return e.abrupt("return",!0);case 3:return e.prev=3,e.next=6,le({key:i.map(function(E){return E.key})});case 6:return t(),p.default.success("\u5220\u9664\u6210\u529F\uFF0C\u5373\u5C06\u5237\u65B0"),e.abrupt("return",!0);case 11:return e.prev=11,e.t0=e.catch(3),t(),p.default.error("\u5220\u9664\u5931\u8D25\uFF0C\u8BF7\u91CD\u8BD5"),e.abrupt("return",!1);case 16:case"end":return e.stop()}},a,null,[[3,11]])}));return function(i){return d.apply(this,arguments)}}(),ce=function(){var a=(0,g.useState)(!1),i=(0,j.Z)(a,2),t=i[0],c=i[1],e=(0,g.useState)(!1),E=(0,j.Z)(e,2),fe=E[0],D=E[1],he=(0,g.useState)(!1),L=(0,j.Z)(he,2),ve=L[0],M=L[1],C=(0,g.useRef)(),pe=(0,g.useState)(),U=(0,j.Z)(pe,2),f=U[0],S=U[1],Fe=(0,g.useState)([]),W=(0,j.Z)(Fe,2),Z=W[0],G=W[1],Y=[{title:"\u89C4\u5219\u540D\u79F0",dataIndex:"name",tip:"\u89C4\u5219\u540D\u79F0\u662F\u552F\u4E00\u7684 key",render:function(n,s){return(0,u.jsx)("a",{onClick:function(){S(s),M(!0)},children:n})}},{title:"\u63CF\u8FF0",dataIndex:"desc",valueType:"textarea"},{title:"\u670D\u52A1\u8C03\u7528\u6B21\u6570",dataIndex:"callNo",sorter:!0,hideInForm:!0,renderText:function(n){return"".concat(n,"\u4E07")}},{title:"\u72B6\u6001",dataIndex:"status",hideInForm:!0,valueEnum:{0:{text:"\u5173\u95ED",status:"Default"},1:{text:"\u8FD0\u884C\u4E2D",status:"Processing"},2:{text:"\u5DF2\u4E0A\u7EBF",status:"Success"},3:{text:"\u5F02\u5E38",status:"Error"}}},{title:"\u4E0A\u6B21\u8C03\u5EA6\u65F6\u95F4",sorter:!0,dataIndex:"updatedAt",valueType:"dateTime",renderFormItem:function(n,s,v){var F=s.defaultRender,m=(0,K.Z)(s,["defaultRender"]),O=v.getFieldValue("status");return"".concat(O)==="0"?!1:"".concat(O)==="3"?(0,u.jsx)(z.Z,(0,y.Z)((0,y.Z)({},m),{},{placeholder:"\u8BF7\u8F93\u5165\u5F02\u5E38\u539F\u56E0\uFF01"})):F(n)}},{title:"\u64CD\u4F5C",dataIndex:"option",valueType:"option",render:function(n,s){return[(0,u.jsx)("a",{onClick:function(){D(!0),S(s)},children:"\u914D\u7F6E"},"config"),(0,u.jsx)("a",{href:"https://procomponents.ant.design/",children:"\u8BA2\u9605\u8B66\u62A5"},"subscribeAlert")]}}];return(0,u.jsxs)(Q.ZP,{children:[(0,u.jsx)(k.ZP,{headerTitle:"\u67E5\u8BE2\u8868\u683C",actionRef:C,rowKey:"key",search:{labelWidth:120},toolBarRender:function(){return[(0,u.jsxs)(R.Z,{type:"primary",onClick:function(){c(!0)},children:[(0,u.jsx)(J.Z,{})," \u65B0\u5EFA"]},"primary")]},request:{},columns:Y,rowSelection:{onChange:function(n,s){G(s)}}}),(Z==null?void 0:Z.length)>0&&(0,u.jsxs)(X.Z,{extra:(0,u.jsxs)("div",{children:["\u5DF2\u9009\u62E9"," ",(0,u.jsx)("a",{style:{fontWeight:600},children:Z.length})," ","\u9879 \xA0\xA0",(0,u.jsxs)("span",{children:["\u670D\u52A1\u8C03\u7528\u6B21\u6570\u603B\u8BA1 ",Z.reduce(function(o,n){return o+n.callNo},0)," \u4E07"]})]}),children:[(0,u.jsx)(R.Z,{onClick:(0,h.Z)(l().mark(function o(){var n,s;return l().wrap(function(F){for(;;)switch(F.prev=F.next){case 0:return F.next=2,oe(Z);case 2:G([]),(n=C.current)===null||n===void 0||(s=n.reloadAndRest)===null||s===void 0||s.call(n);case 4:case"end":return F.stop()}},o)})),children:"\u6279\u91CF\u5220\u9664"}),(0,u.jsx)(R.Z,{type:"primary",children:"\u6279\u91CF\u5BA1\u6279"})]}),(0,u.jsxs)(q.Y,{title:"\u65B0\u5EFA\u89C4\u5219",width:"400px",visible:t,onVisibleChange:c,onFinish:function(){var o=(0,h.Z)(l().mark(function n(s){var v;return l().wrap(function(m){for(;;)switch(m.prev=m.next){case 0:return m.next=2,ie(s);case 2:v=m.sent,v&&(c(!1),C.current&&C.current.reload());case 4:case"end":return m.stop()}},n)}));return function(n){return o.apply(this,arguments)}}(),children:[(0,u.jsx)(V.Z,{rules:[{required:!0,message:"\u89C4\u5219\u540D\u79F0\u4E3A\u5FC5\u586B\u9879"}],width:"md",name:"name"}),(0,u.jsx)(I.Z,{width:"md",name:"desc"})]}),(0,u.jsx)(ae,{onSubmit:function(){var o=(0,h.Z)(l().mark(function n(s){var v;return l().wrap(function(m){for(;;)switch(m.prev=m.next){case 0:return m.next=2,de(s);case 2:v=m.sent,v&&(D(!1),S(void 0),C.current&&C.current.reload());case 4:case"end":return m.stop()}},n)}));return function(n){return o.apply(this,arguments)}}(),onCancel:function(){D(!1),S(void 0)},updateModalVisible:fe,values:f||{}}),(0,u.jsx)(N.Z,{width:600,visible:ve,onClose:function(){S(void 0),M(!1)},closable:!1,children:(f==null?void 0:f.name)&&(0,u.jsx)(_.ZP,{column:2,title:f==null?void 0:f.name,request:(0,h.Z)(l().mark(function o(){return l().wrap(function(s){for(;;)switch(s.prev=s.next){case 0:return s.abrupt("return",{data:f||{}});case 1:case"end":return s.stop()}},o)})),params:{id:f==null?void 0:f.name},columns:Y})})]})},me=ce}}]);