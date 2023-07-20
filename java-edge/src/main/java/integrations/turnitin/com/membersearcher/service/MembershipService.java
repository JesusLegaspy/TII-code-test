package integrations.turnitin.com.membersearcher.service;

import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.function.Function;
import java.util.stream.Collectors;
import integrations.turnitin.com.membersearcher.client.MembershipBackendClient;
import integrations.turnitin.com.membersearcher.model.MembershipList;
import integrations.turnitin.com.membersearcher.model.UserList;
import integrations.turnitin.com.membersearcher.model.User;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class MembershipService {
	@Autowired
	private MembershipBackendClient membershipBackendClient;

	/**
	 * Method to fetch all memberships with their associated user details included.
	 * This method calls out to the php-backend service and fetches all memberships
	 * and all users. After the data for all memberships and all users is completed,
	 * this method associates each membership with its corresponding user.
	 *
	 * @return A CompletableFuture containing a fully populated MembershipList
	 *         object.
	 */
	public CompletableFuture<MembershipList> fetchAllMembershipsWithUsers() {

		CompletableFuture<MembershipList> memberships = membershipBackendClient.fetchMemberships();
		CompletableFuture<UserList> users = membershipBackendClient.fetchUsers();

		return users.thenCombine(memberships, (userList, membershipList) -> {

			Map<String, User> myUserMap = userList.getUsers()
					.stream().collect(Collectors.toMap(User::getId, Function.identity()));

			membershipList.getMemberships().forEach(member -> {
				member.setUser(myUserMap.get(member.getUserId()));
			});

			return membershipList;
		});
	}
}
