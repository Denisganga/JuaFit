import test from 'node:test';
import assert from 'node:assert/strict';
import {readFileSync} from 'node:fs';
import {sourceFacts} from '../kuccps.js';
const data=JSON.parse(readFileSync(new URL('../kuccps_2025.json',import.meta.url)));
test('all 78 pages imported with unique programme codes',()=>{assert.equal(data.records.length,2084);assert.equal(new Set(data.records.map(r=>r.code)).size,2084);assert.equal(data.meta.pageCounts.length,78);assert.ok(data.meta.pageCounts.every(n=>n>0));});
test('every linked programme resolves to a source row and page',()=>{const codes=new Set(data.records.map(r=>r.code));for(const ids of Object.values(data.byCourse)){assert.ok(ids.length);for(const id of ids)assert.ok(codes.has(id));}assert.ok(data.records.every(r=>r.page>=1&&r.page<=78&&r.institution&&r.programme));});
test('architecture requirement reproduces printed cells without historical cutoffs',()=>{const facts=sourceFacts(data,'c_architecture','requirements');assert.match(facts,/MAT A\(121\):C\+/);assert.match(facts,/PHY\(232\):C\+/);assert.match(facts,/1063102/);assert.doesNotMatch(facts,/38\.365/);});
test('verification applies to source edition, not AI suitability or career progression',()=>{assert.equal(sourceFacts(data,'c_cs','progression'),null);assert.equal(sourceFacts(data,'unknown','universities'),null);assert.match(sourceFacts(data,'c_cs','universities'),/not verified by KUCCPS/);});
