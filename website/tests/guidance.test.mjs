import test from 'node:test';
import assert from 'node:assert/strict';
import {cbeQuestions,kcseQuestions,rankPathways,rankCourses,courseGuidance,scoreTags,validAnswer,kcseQuestionsFor} from '../guidance.js';
import {courses} from '../catalog.js';
test('science and engineering answers lead to STEM and a relevant track',()=>{
 const ranked=rankPathways({curious:{selected:[0,1]},subjects:{selected:[0,1]},strength:{selected:[0,1]},learn:{selected:[0]},project:{selected:[0]}});
 assert.equal(ranked[0].id,'stem');assert.ok(ranked[0].score>ranked[1].score);assert.equal(ranked.length,3);
});
test('arts and sports interests lead to Arts & Sports Science',()=>{
 const ranked=rankPathways({curious:{selected:[4,5]},subjects:{selected:[4]},strength:{selected:[4,5]},learn:{selected:[4]},project:{selected:[5]}});
 assert.equal(ranked[0].id,'arts');
});
test('language and enterprise interests lead to Social Sciences',()=>{
 const ranked=rankPathways({curious:{selected:[2,3]},subjects:{selected:[2,3]},strength:{selected:[2,3]},learn:{selected:[2]},project:{selected:[3]}});
 assert.equal(ranked[0].id,'social');
});
test('typed interests contribute to relevant scores',()=>{
 assert.ok(scoreTags(kcseQuestions,{note:{selected:[],text:'I enjoy coding'}}).computing>0);
});
test('course shortlist uses unique real catalog items and responds to technology answers',()=>{
 const results=rankCourses({activities:{selected:[2]},subjects:{selected:[0,5]},skills:{selected:[2,4]},field:{selected:[2]}});
 assert.equal(results.length,5);assert.equal(new Set(results.map(c=>c.id)).size,5);
 assert.ok(results.slice(0,3).some(c=>/comput|software|technology/i.test(c.name)));
 assert.ok(results.every(c=>courses.some(x=>x.id===c.id)));
});
test('every course provides at least five employment areas and progression',()=>{
 for(const course of courses){const g=courseGuidance(course);assert.ok(g.sectors.length>=5,course.name);assert.ok(g.progression.length>=4);assert.ok(g.study.length>=3);}
});
test('education advice includes TSC and conditional postgraduate teaching route',()=>{
 const education=courses.find(c=>/education/i.test(c.name));assert.ok(education);
 const g=courseGuidance(education);assert.ok(g.sectors.some(s=>s.includes('TSC')));assert.ok(g.study.some(s=>s.includes('PhD')));
});
test('secondary flow starts with KCSE performance and skips education level',()=>{assert.equal(kcseQuestions[0].id,'performance');assert.equal(kcseQuestions.length,9);assert.ok(!kcseQuestions.some(q=>q.id==='education'));});

test('subjects require three or four distinct choices',()=>{
 const q=kcseQuestions.find(q=>q.id==='subjects');
 for(const count of [0,1,2,5])assert.equal(validAnswer(q,{selected:Array.from({length:count},(_,i)=>i)}),false);
 for(const count of [3,4])assert.equal(validAnswer(q,{selected:Array.from({length:count},(_,i)=>i)}),true);
});
test('KCSE grade is required',()=>{const q=kcseQuestions[0];assert.equal(validAnswer(q,{selected:[]}),false);assert.equal(validAnswer(q,{selected:[3]}),true);});
test('subjects are secondary subjects without primary or generic placeholders',()=>{const labels=kcseQuestionsFor({}).find(q=>q.id==='subjects').options.map(o=>o.label);for(const name of ['Science','Social Studies','Technical Studies'])assert.ok(!labels.includes(name));for(const name of ['Physics','Home Science','French','Building Construction'])assert.ok(labels.includes(name));});
test('study locations, settings and values have distinct options',()=>{const q=id=>kcseQuestions.find(q=>q.id===id);assert.deepEqual(q('location').options.map(o=>o.label),['In Kenya','Outside Kenya','Either Kenya or abroad','Not sure yet']);assert.equal(q('values').multi,3);assert.ok(q('values').options.some(o=>o.label.startsWith('Integrity')));assert.ok(!q('environment').options.some(o=>o.label==='Working independently'));});
