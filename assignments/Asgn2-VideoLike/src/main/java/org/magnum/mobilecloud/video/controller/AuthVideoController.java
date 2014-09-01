/*
 * 
 * Copyright 2014 Jules White
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *     http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * 
 */

package org.magnum.mobilecloud.video.controller;

import java.security.Principal;
import java.util.Collection;
import java.util.Set;

import org.magnum.mobilecloud.video.client.VideoSvcApi;
import org.magnum.mobilecloud.video.repository.Video;
import org.magnum.mobilecloud.video.repository.VideoRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import com.google.common.collect.Lists;

@Controller
public class AuthVideoController {

	// @ResponseStatus(value = HttpStatus.NOT_FOUND, reason = "No such Video")
	// 404
	// class DataNotFoundException extends RuntimeException {
	// /**
	// *
	// */
	// private static final long serialVersionUID = 2872807321566870087L;
	// }

	@Autowired
	private VideoRepository videos;

	// @GET(VIDEO_SVC_PATH)
	// public Collection<Video> getVideoList();
	//
	// @GET(VIDEO_SVC_PATH + "/{id}")
	// public Video getVideoById(@Path("id") long id);
	//
	// @POST(VIDEO_SVC_PATH)
	// public Video addVideo(@Body Video v);
	//
	// @POST(VIDEO_SVC_PATH + "/{id}/like")
	// public Void likeVideo(@Path("id") long id);
	//
	// @POST(VIDEO_SVC_PATH + "/{id}/unlike")
	// public Void unlikeVideo(@Path("id") long id);
	//
	// @GET(VIDEO_TITLE_SEARCH_PATH)
	// public Collection<Video> findByTitle(@Query(TITLE_PARAMETER) String
	// title);
	//
	// @GET(VIDEO_DURATION_SEARCH_PATH)
	// public Collection<Video>
	// findByDurationLessThan(@Query(DURATION_PARAMETER) long duration);
	//
	// @GET(VIDEO_SVC_PATH + "/{id}/likedby")
	// public Collection<String> getUsersWhoLikedVideo(@Path("id") long id)

	@RequestMapping(value = VideoSvcApi.VIDEO_SVC_PATH, method = RequestMethod.POST)
	public @ResponseBody
	Video addVideo(@RequestBody Video v) {
		v.setLikes(0);
		videos.save(v);
		return v;
	}

	@RequestMapping(value = VideoSvcApi.VIDEO_SVC_PATH, method = RequestMethod.GET)
	public @ResponseBody
	Collection<Video> getVideoList() {
		return Lists.newArrayList(videos.findAll());
	}

	@RequestMapping(value = VideoSvcApi.VIDEO_SVC_PATH + "/{id}", method = RequestMethod.GET)
	public @ResponseBody
	ResponseEntity<Video> getVideoById(@PathVariable("id") long id) {
		Video ret = videos.findOne(id);
		return ret != null ? new ResponseEntity<Video>(ret, HttpStatus.OK)
				: new ResponseEntity<Video>(HttpStatus.NOT_FOUND);
	}

	@RequestMapping(value = VideoSvcApi.VIDEO_SVC_PATH + "/{id}/like", method = RequestMethod.POST)
	public @ResponseBody
	ResponseEntity<String> likeVideo(@PathVariable("id") long id, Principal p) {
		Video video = videos.findOne(id);
		if (video == null)
			return new ResponseEntity<String>(HttpStatus.NOT_FOUND);
		String username = p.getName();
		if (video.getLikedUserNames().contains(username)) {
			return new ResponseEntity<String>(HttpStatus.BAD_REQUEST);
		} else {
			Set<String> likedUserNames = video.getLikedUserNames();
			likedUserNames.add(username);
			video.setLikedUserNames(likedUserNames);
			video.setLikes(likedUserNames.size());
			videos.save(video);
			return new ResponseEntity<String>(HttpStatus.OK);
		}
	}

	@RequestMapping(value = VideoSvcApi.VIDEO_SVC_PATH + "/{id}/unlike", method = RequestMethod.POST)
	public @ResponseBody
	ResponseEntity<String> unlikeVideo(@PathVariable("id") long id, Principal p) {
		Video video = videos.findOne(id);
		if (video == null)
			return new ResponseEntity<String>(HttpStatus.NOT_FOUND);
		String username = p.getName();
		if (!video.getLikedUserNames().contains(username)) {
			return new ResponseEntity<String>(HttpStatus.BAD_REQUEST);
		} else {
			Set<String> likedUserNames = video.getLikedUserNames();
			likedUserNames.remove(username);
			video.setLikedUserNames(likedUserNames);
			video.setLikes(likedUserNames.size());
			videos.save(video);
			return new ResponseEntity<String>(HttpStatus.OK);
		}
	}

	@RequestMapping(value = VideoSvcApi.VIDEO_SVC_PATH + "/{id}/likedby", method = RequestMethod.GET)
	public @ResponseBody
	ResponseEntity<Collection<String>> getUsersWhoLikedVideo(
			@PathVariable("id") long id) {
		Video res = videos.findOne(id);
		if (res == null)
			return new ResponseEntity<Collection<String>>(HttpStatus.NOT_FOUND);
		else
			return new ResponseEntity<Collection<String>>(
					res.getLikedUserNames(), HttpStatus.OK);
	}

	@RequestMapping(value = VideoSvcApi.VIDEO_TITLE_SEARCH_PATH, method = RequestMethod.GET)
	public @ResponseBody
	Collection<Video> findByTitle(@RequestParam("title") String title) {
		return videos.findByName(title);
	}

	@RequestMapping(value = VideoSvcApi.VIDEO_DURATION_SEARCH_PATH, method = RequestMethod.GET)
	public @ResponseBody
	Collection<Video> findByDurationLessThan(
			@RequestParam("duration") Long duration) {
		return videos.findByDurationLessThan(duration);
	}
}
